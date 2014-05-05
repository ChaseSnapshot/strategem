package scatcat.graphics;

/**
 * A channel packer for colors as well as a registry
 * for defining specific colors.
 * 
 * @author R. Matt McCann
 */
public class Color {
    /**
     * A color shouldn't be constructed, it should be registered as 
     * a static member so it can be easily referenced later.
     * 
     * @param red Red channel
     * @param green Green channel
     * @param blue Blue channel
     * @param alpha Alpha channel
     */
    public Color(final float red,
                 final float green,
                 final float blue,
                 final float alpha) {
        mRed   = red;
        mGreen = green;
        mBlue  = blue;
        mAlpha = alpha;
    }

    public final float getAlpha() {
        return mAlpha;
    }
    
    public final float getBlue() {
        return mBlue;
    }
    
    public final float getGreen() {
        return mGreen;
    }
    
    public final float getRed() {
        return mRed;
    }

    private final float mAlpha;
    private final float mBlue;
    private final float mGreen;
    private final float mRed;

    public static final Color ABILITY_BAR_YELLOW 
            = new Color(244.0f / 255.0f, 1.0f, 135.0f / 255.0f, 1.0f);
    public static final Color RED
            = new Color(1.0f, 0.0f, 0.0f, 1.0f);
    public static final Color BLACK
            = new Color(0.0f, 0.0f, 0.0f, 1.0f);
    public static final Color GREEN
            = new Color(0.0f, 1.0f, 0.0f, 1.0f);
    public static final Color HEALTH_BAR_RED 
            = new Color(1.0f, 0.0f, 0.0f, 1.0f);
    public static final Color HUD_BACKGROUND_PANEL_BLUE 
            = new Color(39.0f / 255.0f, 97.0f / 255.0f, 143.0f / 255.0f, 0.7f);
    public static final Color MOVABLE_HIGHLIGHT_BLUE 
            = new Color(39.0f / 255.0f, 97.0f / 255.0f, 143.0f / 255.0f, 0.6f);
    public static final Color SELECTED_HIGHLIGHT_YELLOW
            = new Color(244.0f / 255.0f, 1.0f, 135.0f / 255.0f, 0.6f);
    public static final Color SKY_BLUE 
            = new Color(137.0f / 255.0f, 201.0f / 255.0f, 249.0f / 255.0f, 1.0f);
    public static final Color WHITE 
            = new Color(1.0f, 1.0f, 1.0f, 1.0f);
}
