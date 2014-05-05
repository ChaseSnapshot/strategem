/**
 * 
 */
package scatcat.graphics.shaders;

import android.content.Context;
import android.opengl.GLES20;

import com.google.inject.Inject;
import com.sneaky.stratagem.R;
import scatcat.general.Constants;

/**
 * Shader program for the HUD.
 * 
 * @author R. Matt McCann
 */
public final class SimpleColorShader extends Shader {
    @Inject
    public SimpleColorShader(final Context context,
                             final ShaderRegistry registry) {
        super(context, registry, R.raw.simple_color_vertex_shader,
              R.raw.simple_color_fragment_shader, ATTRIBUTES);
    }

    @Override
    protected void getAttributeHandles() {
        final int programHandle = getProgramHandle();
        
        // Get the uniforms
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");

        // Get the attributes
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
    }
    
    public String getTag() {
        return getClass().toString();
    }
    
    @Override
    public void draw(final int drawMode, final int numVertices) {
        // Pass in the MVP Matrix
        final int matrixCount = 1;
        final boolean willTranspose = false;
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, matrixCount, willTranspose, 
                mMVPMatrix, Constants.BEGINNING_OF_BUFFER);
        
        // Pass in the positions
        final boolean willNormalize = false;
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVBO);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, 
                willNormalize, STRIDE, POSITION_OFFSET);
        
        // Pass in the colors
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mVBO);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT,
                willNormalize, STRIDE, COLOR_OFFSET);
        
        GLES20.glDrawArrays(drawMode, Constants.BEGINNING_OF_BUFFER, numVertices);
    }
    
    /**
     * Sets the MVP matrix using the default settings.
     * @param matrix The MVP matrix
     */
    public void setMVPMatrix(final float[] matrix) {
        mMVPMatrix = matrix;
    }

    /**
     * Sets the active color and position.
     * 
     * @param vbo Handle of the packed VBO.
     */
    public void setVBO(final int vbo) {
        mVBO = vbo;
    }
    
    private static final String[] 	ATTRIBUTES = new String[] {"a_Position", "a_Color"};
    private static final int        POSITION_OFFSET = 0;
    private static final int        POSITION_DATA_SIZE = 3;
    private static final int        COLOR_OFFSET = POSITION_DATA_SIZE * Constants.BYTES_PER_FLOAT;
    private static final int        COLOR_DATA_SIZE = 4;
    private static final int        STRIDE = (POSITION_DATA_SIZE + COLOR_DATA_SIZE) * Constants.BYTES_PER_FLOAT;
    
    private int                      mColorHandle;
    private float[]                  mMVPMatrix;
    private int                      mMVPMatrixHandle;
    private int                      mPositionHandle;
    private int                      mVBO;
}
