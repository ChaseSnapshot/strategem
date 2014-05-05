package com.sneaky.stratagem;

import com.google.inject.Injector;
import com.sneaky.stratagem.flow.GameFlowController;

import android.opengl.GLES20;
import scatcat.graphics.MVP;
import scatcat.input.ClickHandler;
import scatcat.input.Draggable;
import scatcat.input.LongPressHandler;
import scatcat.input.ZoomHandler;

public abstract class StratagemRenderer implements ClickHandler, LongPressHandler, Draggable, ZoomHandler  {
    private final Injector injector;
    
    public StratagemRenderer(final Injector injector) {
        this.injector = injector;
        
        // Enable transparency blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        
        // Activate the game flow controller
        GameFlowController controller = injector.getInstance(GameFlowController.class);
        controller.start();
    }
    
    /** Cleans up the resources allocated by the renderer. */
    public void close() {
        injector.getInstance(GameFlowController.class).setIsRunning(false);
    }
    
    /** Draw the current frame. */
    public abstract void drawFrame(final MVP mvp);
}
