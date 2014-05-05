package com.sneaky.stratagem;

import com.google.inject.Injector;

import scatcat.general.Constants;
import scatcat.graphics.MVP;

import android.opengl.Matrix;

public abstract class StratagemRenderer2D extends StratagemRenderer {
    /** The initial Model-View-Projection stack used for rendering. */
    private final float[] projectionMatrix;
    
    protected StratagemRenderer2D(final Injector injector) {
        super(injector);
        
        // Set up the parameters of the projection matrix
        final float near = -1.0f;
        final float far = 1.0f;
        final float left = -0.5f;
        final float right = 0.5f;
        final float top = 0.5f;
        final float bottom = -0.5f;
        
        // Calculate the projection matrix
        projectionMatrix = new float[Constants.MATRIX_SIZE];
        Matrix.orthoM(projectionMatrix, Constants.NO_OFFSET, left, right, bottom, top, near, far);
    }
    
    /** {@inheritDoc} */
    @Override
    public void drawFrame(final MVP mvp) {
        mvp.push(MVP.Type.PROJECTION, projectionMatrix);
    }
}
