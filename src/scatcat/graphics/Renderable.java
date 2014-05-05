package scatcat.graphics;

/**
 * Interface for objects that can be rendered graphically.
 * @author R. Matt McCann
 */
public interface Renderable {
    /**
     * The model-projection matrix to base rendering off of.
     * @param modelProjectionMatrix The combined model-projection matrix
     * @param shaderPositionHandle Reference to the position variable in the shader
     * @param shaderTexCoordHandle Reference to the texture coordinate variable in the shader
     * @param shaderTextureHandle Reference to the texture variable in the shader
     */
    void render(final float[] modelViewMatrix, final float[] projectionMatrix);
}
