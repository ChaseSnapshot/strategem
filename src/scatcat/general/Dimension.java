package scatcat.general;

/**
 * Holds 2D spatial details.
 * 
 * @author R. Matt McCann
 */
public class Dimension {
    /**
     * Constructor.
     */
    public Dimension() {
        mHeight = 0.0f;
        mWidth = 0.0f;
    }
    
    /**
     * Constructor.
     * 
     * @param height Height.
     * @param width Width.
     */
    public Dimension(final float height,
                     final float width
                     ) {
        mHeight = height;
        mWidth = width;
    }
    
    public final float getHeight() {
        return mHeight;
    }
    
    public final float getWidth() {
        return mWidth;
    }
    
    public final void setHeight(final float height) {
        mHeight = height;
    }
    
    public final void setWidth(final float width) {
        mWidth = width;
    }
    
    private float mHeight;
    private float mWidth;
}
