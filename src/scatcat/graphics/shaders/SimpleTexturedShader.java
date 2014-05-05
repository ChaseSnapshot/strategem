package scatcat.graphics.shaders;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import scatcat.general.Constants;
import scatcat.graphics.DrawUtils;

import com.sneaky.stratagem.R;

import android.content.Context;
import android.opengl.GLES20;

/**
 * Shader program for the HUD.
 * 
 * @author R. Matt McCann
 */
@Singleton
public final class SimpleTexturedShader extends Shader {
    @Inject
    protected SimpleTexturedShader(final Context context,
                                   final ShaderRegistry registry) {
        super(context, registry, R.raw.simple_textured_vertex_shader,
              R.raw.simple_textured_fragment_shader, ATTRIBUTES);
    }
    
    @Override
    protected void getAttributeHandles() {
        final int programHandle = getProgramHandle();
        
        // Get the uniforms
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mTextureHandle = GLES20.glGetUniformLocation(programHandle, "u_Texture");
        
        // Get the attributes
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mTexCoordHandle = GLES20.glGetAttribLocation(programHandle, "a_TexCoord");
    }

    @Override
    public void draw(final int drawMode, final int numVertices) {
        final int vbo;
        if (mVBO.isPresent()) {
            vbo = mVBO.get();
        } else {
            vbo = DrawUtils.getUnitSquarePtVbo();
        }
        
        // Pass in the MVP matrix
        final int matrixCount = 1;
        final boolean willTranspose = false;
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, matrixCount, willTranspose, 
                mMVPMatrix, Constants.BEGINNING_OF_BUFFER);
        
        // Pass in the texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture);
        final int activeTexture = 0;
        GLES20.glUniform1i(mTextureHandle, activeTexture);
        
        // Pass in the positions
        final boolean willNormalize = false;
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, 
                willNormalize, STRIDE, Constants.BEGINNING_OF_BUFFER);
        
        // Pass in the texture coordinates
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo);
        GLES20.glEnableVertexAttribArray(mTexCoordHandle);
        GLES20.glVertexAttribPointer(mTexCoordHandle, TEX_COORD_DATA_SIZE, GLES20.GL_FLOAT, 
                willNormalize, STRIDE, TEX_COORD_OFFSET);
        
        GLES20.glDrawArrays(drawMode, Constants.BEGINNING_OF_BUFFER, numVertices);
        
        // Clean up the vbo
        mVBO = Optional.<Integer>absent();
    }
    
    public String getTag() {
        return getClass().toString();
    }
    
    /**
     * Sets the MVP matrix using the default settings.
     * @param matrix The MVP matrix
     */
    public void setMVPMatrix(final float[] matrix) {
        mMVPMatrix = matrix;
    }

    /**
     * Sets the input texture.
     * @param texture The input texture.
     */
    public void setTexture(final int texture) {
        mTexture = texture;
    }
    
    public void setVBO(final int vbo) { this.mVBO = Optional.<Integer>of(vbo); }
    
    private static final String[] ATTRIBUTES = 
        new String[] {"a_Position", "a_TexCoord"};
    private static final int POSITION_DATA_SIZE = 3;
    private static final int TEX_COORD_OFFSET = POSITION_DATA_SIZE * Constants.BYTES_PER_FLOAT;
    private static final int TEX_COORD_DATA_SIZE = 2;
    private static final int STRIDE = (POSITION_DATA_SIZE + TEX_COORD_DATA_SIZE) * Constants.BYTES_PER_FLOAT;
    
    private float[]                     mMVPMatrix;
    private int                         mMVPMatrixHandle;
    private int                         mPositionHandle;
    private int                         mTexCoordHandle;
    private int                         mTexture;
    private int                         mTextureHandle;
    private Optional<Integer>           mVBO = Optional.<Integer>absent();
}
