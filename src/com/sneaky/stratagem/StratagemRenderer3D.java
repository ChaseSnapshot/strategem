package com.sneaky.stratagem;

import com.google.inject.Injector;

import scatcat.general.Constants;
import scatcat.graphics.MVP;
import android.opengl.GLES20;
import android.opengl.Matrix;

public abstract class StratagemRenderer3D extends StratagemRenderer {
    /** The initial Model-View-Projection stack used for rendering. */
    private final float[] projectionMatrix;
    
    protected StratagemRenderer3D(final Injector injector) {
        super(injector);
        
        // Enable the depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);
        GLES20.glClearDepthf(1.0f);
        
        // Set up the parameters of the projection matrix
        final float near   = -10000.0f;
        final float far    = 10000.0f;
        final float left   = -0.5f;
        final float right  = 0.5f;
        final float top    = 0.5f;
        final float bottom = -0.5f;
        
        // Create the perspective projection matrix
        projectionMatrix = new float[Constants.MATRIX_SIZE];
        Matrix.orthoM(projectionMatrix, Constants.NO_OFFSET, left, right, bottom, top, near, far);
    }
    
    /** {@inheritDoc} */
    @Override
    public void drawFrame(final MVP mvp) {
        mvp.push(MVP.Type.PROJECTION, projectionMatrix);
    }
    
    protected float[] getProjectionMatrix() { return projectionMatrix; }
}
