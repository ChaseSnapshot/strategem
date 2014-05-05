package com.sneaky.stratagem.graphics.textures;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;


/**
 * Texture package for units.
 * 
 * @author R. Matt McCann
 */
public class UnitTexturePack extends TexturePack {
    private final TextureFactory textureFactory;
    
    /**
     * Constructor.
     * 
     * @param profileImage Android resource handle for profile image.
     * @param northImage Android resource handle for north image.
     * @param eastImage Android resource handle for east image.
     * @param southImage Android resource handle for south image.
     * @param westImage Android resource handle for west image.
     */
    @Inject
    public UnitTexturePack(final TextureFactory textureFactory,
                           @Assisted("ProfileImage") final int profileImage, 
                           @Assisted("NorthImage") final int northImage, 
                           @Assisted("EastImage") final int eastImage,
                           @Assisted("SouthImage") final int southImage, 
                           @Assisted("WestImage") final int westImage) {
        super();
        
        this.textureFactory = textureFactory;
        
        mProfileImage = profileImage;
        mNorthImage = northImage;
        mEastImage = eastImage;
        mSouthImage = southImage;
        mWestImage = westImage;
        
        setNumTextures(NUM_TEXTURES);
    }
    public interface UnitTexturePackFactory {
        UnitTexturePack create(@Assisted("ProfileImage") final int profileImage, 
                               @Assisted("NorthImage") final int northImage, 
                               @Assisted("EastImage") final int eastImage,
                               @Assisted("SouthImage") final int southImage, 
                               @Assisted("WestImage") final int westImage);
    }

    public final int getProfile() {
        return getTextureHandle(PROFILE);
    }

    public final int getNorth() {
        return getTextureHandle(NORTH);
    }

    public final int getEast() {
        return getTextureHandle(EAST);
    }

    public final int getSouth() {
        return getTextureHandle(SOUTH);
    }

    public final int getWest() {
        return getTextureHandle(WEST);
    }

    /** {@inheritDoc} */
    @Override
    public final void loadTextures() {
        setTextureHandle(PROFILE, textureFactory.loadTexture(mProfileImage));
        setTextureHandle(NORTH, textureFactory.loadTexture(mNorthImage));
        setTextureHandle(EAST, textureFactory.loadTexture(mEastImage));
        setTextureHandle(SOUTH, textureFactory.loadTexture(mSouthImage));
        setTextureHandle(WEST, textureFactory.loadTexture(mWestImage));
    }

    private static final int NUM_TEXTURES = 5;
    private static final int PROFILE = 0;
    private static final int NORTH = 1;
    private static final int EAST = 2;
    private static final int SOUTH = 3;
    private static final int WEST = 4;
    
    private int   mEastImage;
    private int   mNorthImage;
    private int   mProfileImage;
    private int   mSouthImage;
    private int   mWestImage;
}
