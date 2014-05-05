package com.sneaky.stratagem.graphics.textures;

import static com.google.common.base.Preconditions.checkArgument;
import com.google.inject.Inject;

import scatcat.general.Dimension;
import scatcat.general.DrawMath;
import scatcat.graphics.Color;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * Helper class providing common functionality related to textures.
 * 
 * @author R. Matt McCann
 */
public final class TextureFactory {
    /** Application context. */
    private final Context context;
    
    /** Guice injection constructor. */
    @Inject
    public TextureFactory(final Context context) {
        this.context = context;
    }
    
    /**
     * Loads a texture into the graphics engine for later use.
     * @param resourceID The android resource ID of the texture to be loaded.
     * @return Handle referencing the texture in OpenGL
     */
    public int loadTexture(final int resourceID) {
        // Disable the default android image pre-scaling
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        
        // Load the image from the file system
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceID, options);

        // Load the texture
        int textureHandle = loadTexture(bitmap);
        
        // Clean up
        bitmap.recycle();
        
        return textureHandle;
    }
    
    /**
     * Loads a texture into the graphics engine for later use.
     * @param bitmap The image to be loaded as a texture
     * @return Handle referencing the texture in OpenGL
     */
    public int loadTexture(final Bitmap bitmap) {
        final int[] textureHandle = new int[1];
        
        // Generate the texture handle
        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] > 0) {
            // Set the texture handle as active
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            
            // Set the filtering options
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            
            // Load the image into the graphics engine
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        } else {
            throw new RuntimeException("Error loading texture.");
        }
        
        return textureHandle[0];
    }
    
    public int texturizeText(final String text,
                             final Color color,
                             final Paint.Align align,
                             final float fontSize,
                             float[] aspectRatio) {
        checkArgument(text != null, "Text must not be null!");
        checkArgument(color != null, "Color must not be null!");
        checkArgument(align != null, "Align must not be null!");
        checkArgument(aspectRatio != null, "AspectRatio must not be null!");
        checkArgument(aspectRatio.length == 1, "AspectRatio length must be 1, got %s", aspectRatio.length);
        
        Dimension textSize = new Dimension();
        
        final int textureID = texturizeText(text, color, align, fontSize, textSize);
        aspectRatio[0] = textSize.getWidth() / textSize.getHeight();
        
        return textureID;
    }
    
    /**
     * Generates and loads into the graphics engine a texture that
     * displays the given text as well as returning the texture
     * vertices for displaying the text's texture.
     * 
     * @param text The string to texturize.
     * @param color The font color.
     * @param align Alignment of the text within the texture.
     * @param fontSize Desired font size.
     * @param textSize Final text dimensions.
     * @return Texture handle.
     */
    public int texturizeText(final String text, 
                             final Color color, 
                             final Paint.Align align,
                             final float fontSize, 
                             Dimension textSize) { //TODO WTF WTF WTF
        // Set up the attributes of the text
        Paint textPaint = new Paint();
        textPaint.setTextSize(fontSize);
        textPaint.setAntiAlias(true);
        textPaint.setTypeface(Typeface.SANS_SERIF);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(align);
        textPaint.setARGB((int) color.getAlpha() * 255, (int) color.getRed() * 255, 
                          (int) color.getGreen() * 255, (int) color.getBlue() * 255);
        
        // Create the bitmap onto which the text will be written
        float textHeight = DrawMath.findCeilingPowerOfTwo(fontSize);
        float textWidth = DrawMath.findCeilingPowerOfTwo(textPaint.measureText(text));
        textSize.setHeight(textHeight);
        textSize.setWidth(textWidth);
        Bitmap bitmap = Bitmap.createBitmap((int) textWidth, (int) textHeight, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(bitmap);
        bitmap.eraseColor(0);

        // Draw the text onto the bitmap
        Rect bounds = new Rect();
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        float yPos = textHeight / 2 + Math.abs(bounds.bottom + bounds.top) / 2;
        if (align == Paint.Align.LEFT) {
            canvas.drawText(text, bounds.left, yPos, textPaint);
        } else if (align == Paint.Align.CENTER) {
            canvas.drawText(text, textWidth / 2, yPos, textPaint);
        } else { // align == Paint.Align.RIGHT
            canvas.drawText(text, textWidth, yPos, textPaint);
        }
            
        // Load the texture into the graphics engine
        int resultTextureHandle = loadTexture(bitmap);
        
        /*if (text.equals("8")) {
            try {
                FileOutputStream out = new FileOutputStream("/sdcard/HullabalooTest.jpg");
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        
        // Clean up
        bitmap.recycle();
        
        return resultTextureHandle;
    }
}
