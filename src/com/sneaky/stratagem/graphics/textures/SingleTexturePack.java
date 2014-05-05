package com.sneaky.stratagem.graphics.textures;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;


/**
 * Encapsulate a single texture. The class wrapper
 * is used to ensure that a given texture is only ever
 * loaded into the graphics engine once.
 * 
 * @author R. Matt McCann
 */
public class SingleTexturePack extends TexturePack {
    private final TextureFactory textureFactory;
    
    /** Guice injection constructor. */
    @Inject
    protected SingleTexturePack(final TextureFactory textureFactory,
                                @Assisted final int textureImage) {
        this.textureFactory = textureFactory;
    
        mTextureImage = textureImage;
        setNumTextures(NUM_TEXTURES);
    }
    public interface SingleTexturePackFactory {
        SingleTexturePack create(int textureImage);
    }

    /**
     * Returns a handle to the texture.
     * 
     * @return The texture handle.
     */
    public final int getTexture() {
        return getTextureHandle(0);
    }
    
    /** {@inheritDoc} */
    @Override
    public final void loadTextures() {
        setTextureHandle(0, textureFactory.loadTexture(mTextureImage));
    }
    
    private static final int NUM_TEXTURES = 1;
    private int mTextureImage;
}
