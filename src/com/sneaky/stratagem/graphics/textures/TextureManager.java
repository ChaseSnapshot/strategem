package com.sneaky.stratagem.graphics.textures;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton ensuring every texture is only ever loaded into the graphics engine
 * once. This is accomplished by tracking each texture and the corresponding
 * objects the reference them.
 * 
 * @author R. Matt McCann
 */
public final class TextureManager {
    /**
     * Private constructor. TextureTracker is a singleton so it is not available.
     */
    private TextureManager() { }
    
    /** 
     * Decreases the reference count for a unique ID by one.
     * 
     * @param id    ID to decrement
     */
    public void decrementRefCount(final String id) {
        TexturePack texturePack = mRegister.get(id);
        texturePack.decrementRefCount();
    }
    
    /**
     * Provides access to this singleton object.
     * @return  A singleton TextureTracker
     */
    public static TextureManager getInstance() {
        if (mInstance == null) {
            mInstance = new TextureManager();
        }
        
        return mInstance;
    }

    /**
     * Returns a previously registered texture pack.
     * 
     * @param id    ID of the texture pack
     * @return      Texture pack
     */
    public TexturePack getTexturePack(final String id) {
        return mRegister.get(id);
    }
    
    /**
     * Increases the reference count for a unique ID by one.
     * 
     * @param id    ID to increment
     */
    public void incrementRefCount(final String id) {
        TexturePack texturePack = mRegister.get(id);
        
        texturePack.incrementRefCount();
    }
    
    /**
     * Checks if a texture pack is associated with the unique id.
     * 
     * @param id    Unique ID to check for association
     * @return      Whether or not the id is already registered
     */
    public boolean isRegistered(final String id) {
        return mRegister.containsKey(id);
    }
    
    /**
     * Starts tracking the provided id-texture pack pair. To prevent
     * duplicate textures from occurring, isRegistered should be
     * checked before calling.
     * 
     * @param id            The unique key for the texture pack
     * @param texturePack   The pack of textures
     */
    public void register(final String id,
                         final TexturePack texturePack) {
        if (mRegister.containsKey(id)) {
            throw new RuntimeException(TAG + ": Unique ID('" + id 
                                        + "') already has a texture pack associated with it.");
        }
        
        mRegister.put(id, texturePack);
    }
    
    /**
     * Reloads all the textures into the graphics engine
     * after a context loss (i.e. app paused, home button pressed, etc.).
     */
    public void reload() {
        for (String id : mRegister.keySet()) {
            TexturePack pack = mRegister.get(id);
            if (pack.getRefCount() > 0) {
                pack.loadTextures();
            }
        }
    }

    private static final String TAG = "TextureManager";
    
    private static TextureManager    mInstance;
    private Map<String, TexturePack> mRegister = new HashMap<String, TexturePack>();
}
