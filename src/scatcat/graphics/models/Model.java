package scatcat.graphics.models;

import com.google.inject.Inject;

import android.opengl.GLES20;
import android.opengl.Matrix;
import scatcat.general.Constants;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.shaders.SimpleTexturedShader;

public class Model implements RenderableMVP {
    /** Handle referencing the VBO storing all of the raw data. */
    private int dataVBOHandle;
    
    /** # of vertices comprising the model. */
    private int numVertices;
    
    private final SimpleTexturedShader shader;
    
    /** Handle referencing the texture map used for rendering the model. */
    private int textureHandle;
    
    /** # of data points used for a vertex position. */
    public static final int POSITION_DATA_SIZE = 3;
    
    /** Stride distance between position data
    
    /** # of data points used for a vertex's texture coordinate mapping. */
    public static final int TEXTURE_COORD_DATA_SIZE = 2;
    
    @Inject
    protected Model(final SimpleTexturedShader shader) {
        this.shader = shader;
    }
    
    @Override
    public void render(MVP mvp) {
        shader.activate();
        
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        
        final float ridgeFactor = 1.45f;
        Matrix.scaleM(model, Constants.NO_OFFSET, ridgeFactor, ridgeFactor, ridgeFactor);
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, -0.057f, 0.0f); //2.5f / 21.5f, 0.0f);
        //Matrix.translateM(model, Constants.NO_OFFSET, 1., y, z)
        //Matrix.scaleM(model, Constants.NO_OFFSET, 1.1f, 1.1f, 1.1f);
        
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(textureHandle);
        shader.setVBO(dataVBOHandle);
        shader.draw(GLES20.GL_LINES, numVertices);
    }
    
    public final void setDataVBOHandle(int dataVBOHandle) { this.dataVBOHandle = dataVBOHandle; }
    public final void setNumVertices(int numVertices) { this.numVertices = numVertices; }
    public final void setTextureHandle(int textureHandle) { this.textureHandle = textureHandle; }
}