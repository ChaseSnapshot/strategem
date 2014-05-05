package com.sneaky.stratagem.graphics.textures;

import scatcat.general.Constants;

import android.opengl.GLES20;

/**
 * Collection of texture rendering details that can be associated with renderable objects.
 * 
 * @author R. Matt McCann
 */
public abstract class TexturePack {
    /** Hidden Constructor. */
    protected TexturePack() { }
    
    /**
     * Decreases the reference count. If the count reaches zero,
     * the texture is unloaded.
     */
    public final void decrementRefCount() {
        mReferenceCount--;
        if (mReferenceCount == 0) {
            GLES20.glDeleteTextures(mNumTextures, mTextureHandles, Constants.NO_STRIDE);
        }
    }

    public final int getRefCount() {
        return mReferenceCount;
    }
    
    /**
     * Returns the requested texture handle.
     * 
     * @param pos The handle to get
     * @return The handle requested
     */
    protected final int getTextureHandle(final int pos) {
        return mTextureHandles[pos];
    }
    
    /**
     * Increases the reference count. If the count is zero, the
     * textures are loaded into the graphics engine.
     */
    public final void incrementRefCount() {
        if (mReferenceCount == 0) {
            loadTextures();
        }
        
        mReferenceCount++;
    }

    /**
     * Loads the textures into the graphics engine.
     */
    public abstract void loadTextures();

    /**
     * Sets the number of textures and allocates sufficient
     * array space for the handles.
     * 
     * @param numTextures Number of textures to set
     */
    protected final void setNumTextures(final int numTextures) {
        mNumTextures = numTextures;
        mTextureHandles = new int[mNumTextures];
    }
    
    /**
     * Sets the texture handle value.
     * 
     * @param pos Texture handle to set
     * @param value Texture handle value
     */
    protected final void setTextureHandle(final int pos, final int value) {
        mTextureHandles[pos] = value;
    }
    
    private int   mNumTextures;
    private int   mReferenceCount;
    private int[] mTextureHandles;
}
