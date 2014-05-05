package com.sneaky.stratagem.proxy;

import scatcat.general.Device;
import scatcat.general.DrawMath;
import scatcat.general.points.CartesianScreenPoint2D;
import scatcat.general.points.PointFactory;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Handles all of the user input, passing it to the proxying renderer.
 * 
 * @author R. Matt McCann
 */
@SuppressLint("ViewConstructor")
public class ProxyView extends GLSurfaceView {
    /** How far the movement action has to drag before being classified as a drag gesture. */
    private final float dragActivationDistance = 10.0f;
    
    /** Whether or not the action has been classified as a drag gesture. */
    private boolean isDragAction = false;
    
    /** Last location touched on the screen. Used for calculating gesture vectors. */
    private CartesianScreenPoint2D lastTouchLocation;
    
    /** Used to translate input events into normalized screen points. */
    private final PointFactory pointFactory;
    
    /** Used for handling drag related input. */
    private final ProxyRenderer renderer;
    
    /** Timing thread used to detect long press events. */
    private PressTimer pressTimer;
    
    /** Detects simple gestures. */
    //private final GestureDetector simpleGestureDetector;
    
    /** Detects zoom gestures. */
    private final ScaleGestureDetector zoomGestureDetector;
    
    /** Constructor. */
    public ProxyView(final Activity activity,
                     final ProxyRenderer renderer) {
        super(activity.getApplicationContext());
        
        this.pointFactory = new PointFactory(new Device(activity));
        this.renderer = renderer;
        
        // Set the OpenGL context to be preserved when the application is paused
        setPreserveEGLContextOnPause(true);
        
        // Enable OpenGL ES 2.0
        setEGLContextClientVersion(2);
        
        // Bind the renderer
        setRenderer(renderer);
        
        // Enable manual rendering control by the game loop controller
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        // Set up the input detectors
        this.zoomGestureDetector = new ScaleGestureDetector(
                activity.getApplicationContext(), new ZoomGestureDetector(renderer));
    }
    
    /** {@inheritDoc} */
    @Override
    public synchronized final boolean onTouchEvent(final MotionEvent event) {
        final CartesianScreenPoint2D touchLocation = pointFactory.createCartesianScreenPoint2D(event);
        
        // Check if a scale gesture occurred
        zoomGestureDetector.onTouchEvent(event);
        
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                lastTouchLocation = touchLocation;
                isDragAction = false;
                
                pressTimer = new PressTimer(renderer, System.currentTimeMillis(), 
                        pointFactory.createNormalizedPoint2D(touchLocation));
                new Thread(pressTimer).start();
                
                break;
            case MotionEvent.ACTION_MOVE:
                if (((DrawMath.calcDistance(lastTouchLocation, touchLocation) > dragActivationDistance) || isDragAction)) {
                    pressTimer.setIsShuttingDown(true);
                    
                    if (!isDragAction) {
                        isDragAction = renderer.handlePickUp(pointFactory.createNormalizedPoint2D(touchLocation));
                    }
                    
                    CartesianScreenPoint2D moveVector = touchLocation.subtract(lastTouchLocation);
                    lastTouchLocation = touchLocation;
                    
                    if (isDragAction) {
                        return renderer.handleDrag(pointFactory.createNormalizedPoint2D(moveVector));
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                pressTimer.setIsShuttingDown(true);
                
                // If the action was long press
                if (pressTimer.isLongPress()) {
                    return true;
                // If the action was a drag gesture
                } else if (isDragAction) {
                    isDragAction = false;
                    return renderer.handleDrop(pointFactory.createNormalizedPoint2D(touchLocation));
                }
                // Otherwise the action was a simple click gesture
                else {
                    return renderer.handleClick(pointFactory.createNormalizedPoint2D(touchLocation));
                }
        }
        
        return true;
    }
    
    /** Simple zoom gesture detector. */
    final class ZoomGestureDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private final ProxyRenderer handler;
        
        public ZoomGestureDetector(final ProxyRenderer handler) {
            this.handler = handler;
        }
        
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.d("Input", "Zoom of scale " + detector.getScaleFactor());
            return handler.handleZoom(detector.getScaleFactor());
        }
    }
}
