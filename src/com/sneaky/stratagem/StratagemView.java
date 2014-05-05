package com.sneaky.stratagem;

import scatcat.general.points.NormalizedPoint2D;
import scatcat.input.ClickHandler;
import scatcat.input.LongPressHandler;
import scatcat.input.ZoomHandler;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Base class for all activities' views. Handles identifying which input action took place
 * and hands those actions off the sub-class callbacks.
 * 
 * @author R. Matt McCann
 */
public abstract class StratagemView extends GLSurfaceView implements ClickHandler, LongPressHandler, ZoomHandler {
    /** Detects simple gestures. */
    private final GestureDetector simpleGestureDetector;
    
    /** Detects zoom gestures. */
    private final ScaleGestureDetector zoomGestureDetector;
    
    /** Constructor. */
    public StratagemView(final Context context,
                         final GLSurfaceView.Renderer renderer) {
        super(context);
        
        // Set the OpenGL context to be preserved when the application is paused
        setPreserveEGLContextOnPause(true);
        
        // Enable OpenGL ES 2.0
        setEGLContextClientVersion(2);
        
        // Bind the renderer
        setRenderer(renderer);
        
        // Enable manual rendering control by the game loop controller
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
        // Set up the input detectors
        simpleGestureDetector = new GestureDetector(context, new SimpleGestureDetector(this));
        zoomGestureDetector = new ScaleGestureDetector(context, new ZoomGestureDetector(this));
    }
    
    /** {@inheritDoc} */
    @Override
    public final boolean onTouchEvent(final MotionEvent event) {
        // Check if a simple gesture occurred
        if (simpleGestureDetector.onTouchEvent(event)) { 
            return true;
        }
        
        // Check if a scale gesture occurred
        if (zoomGestureDetector.onTouchEvent(event)) {
            return true;
        }
        
        return false;
    }
    
    /** Simple long press gesture detector. */
    final class SimpleGestureDetector extends GestureDetector.SimpleOnGestureListener {
        final StratagemView handler;
        
        public SimpleGestureDetector(final StratagemView handler) {
            this.handler = handler;
        }
        
        @Override
        public void onLongPress(final MotionEvent event) {
            Log.d("Input", "Long Press at (" + event.getX() + "," + event.getY());
            handler.handleLongPress(new NormalizedPoint2D(event));
        }
        
        @Override
        public boolean onSingleTapConfirmed(final MotionEvent event) {
            Log.d("Input", "Single Tap at (" + event.getX() + "," + event.getY());
            return handler.handleClick(new NormalizedPoint2D(event));
        }
    }
    
    /** Simple zoom gesture detector. */
    final class ZoomGestureDetector extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private final ZoomHandler handler;
        
        public ZoomGestureDetector(final ZoomHandler handler) {
            this.handler = handler;
        }
        
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.d("Input", "Zoom of scale " + detector.getScaleFactor());
            return handler.handleZoom(detector.getScaleFactor());
        }
    }
}
