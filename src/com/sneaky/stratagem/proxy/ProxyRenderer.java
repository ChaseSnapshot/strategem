package com.sneaky.stratagem.proxy;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sneaky.stratagem.StratagemRenderer;

import scatcat.general.points.NormalizedPoint2D;
import scatcat.graphics.MVP;
import scatcat.input.ClickHandler;
import scatcat.input.Draggable;
import scatcat.input.LongPressHandler;
import scatcat.input.ZoomHandler;
import com.sneaky.stratagem.opening.OpeningMenuModule;
import com.sneaky.stratagem.opening.OpeningMenuRenderer;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

public class ProxyRenderer implements ClickHandler, Draggable, GLSurfaceView.Renderer, LongPressHandler, ZoomHandler {
    private final ProxyActivity activity;
    
    private StratagemRenderer currentRenderer;
    
    private ProxyView view;
    
    public ProxyRenderer(final ProxyActivity activity) {
        this.activity = activity;
    }
    
    public ProxyActivity getActivity() { return activity; }
    
    public Context getContext() { return activity.getApplicationContext(); }
    
    public ProxyView getView() { return view; } 
    
    @Override
    public boolean handleClick(NormalizedPoint2D clickLocation) {
        return currentRenderer.handleClick(clickLocation);
    }
    
    @Override
    public boolean handleDrag(NormalizedPoint2D moveVector) {
        return currentRenderer.handleDrag(moveVector);
    }

    @Override
    public boolean handleDrop(NormalizedPoint2D dropLocation) {
        return currentRenderer.handleDrop(dropLocation);
    }
    
    @Override
    public boolean handleLongPress(NormalizedPoint2D pressLocation) {
        return currentRenderer.handleLongPress(pressLocation);
    }
    
    @Override
    public boolean handlePickUp(NormalizedPoint2D touchLocation) {
        return currentRenderer.handlePickUp(touchLocation);
    }
    
    @Override
    public boolean handleZoom(float zoomFactor) {
        return currentRenderer.handleZoom(zoomFactor);
    }
    
    @Override
    public synchronized void onDrawFrame(GL10 arg0) {
        // Draw the background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        
        currentRenderer.drawFrame(new MVP());
    }

    @Override
    public void onSurfaceChanged(GL10 arg0, int width, int height) {
        // Set up the view-port
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
        Injector injector = Guice.createInjector(new OpeningMenuModule(this));
        currentRenderer = injector.getInstance(OpeningMenuRenderer.class);
    }
    
    public synchronized void setRenderer(final StratagemRenderer renderer) {
        currentRenderer.close();
        currentRenderer = renderer;
    }

    public void setView(final ProxyView view) { this.view = view; }
}
