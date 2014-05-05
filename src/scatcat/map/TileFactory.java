package scatcat.map;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.sneaky.stratagem.R;
import com.sneaky.stratagem.graphics.textures.SingleTexturePack;
import com.sneaky.stratagem.graphics.textures.TextureFactory;
import com.sneaky.stratagem.graphics.textures.TextureManager;
import com.sneaky.stratagem.graphics.textures.SingleTexturePack.SingleTexturePackFactory;

/**
 * Factory class that abstracts away the details of constructing different types of tiles.
 * 
 * @author R. Matt McCann
 */
public final class TileFactory {
    private final Injector injector;
    
    private final SingleTexturePackFactory singleTexturePackFactory;
    
    private final TextureFactory textureFactory;
    
    /** Guice injection constructor. */
    @Inject
    protected TileFactory(final Injector injector,
                          final SingleTexturePackFactory singleTexturePackFactory,
                          final TextureFactory textureFactory) { 
        this.injector = injector;
        this.singleTexturePackFactory = singleTexturePackFactory;
        this.textureFactory = textureFactory;
    }
    
    /**
     * Construction method. Produces a tile of the requested type.
     * 
     * @param type Type of tile to construct.
     * @return Constructed tile.
     */
    public Tile create(final Type type) {
        switch(type) {
            case GRASS:
                return createGrass();
            default:
                throw new RuntimeException("Unsupported tile type!");
        }
    }
    
    /**
     * Creates the simple grass tile.
     * 
     * @return Simple grass tile
     */
    private Tile createGrass() {
        /*SingleTexturePack texturePack;
        
        TextureManager manager = TextureManager.getInstance();
        if (!manager.isRegistered("TILE.GRASS")) {
            texturePack = singleTexturePackFactory.create(R.drawable.grass);
            manager.register("TILE.GRASS", texturePack);
        } else {
            texturePack = (SingleTexturePack) manager.getTexturePack("TILE.GRASS");
        }
        texturePack.incrementRefCount();*/
        
        Tile grass = injector.getInstance(Tile.class);
        grass.setSurfaceTexture(injector.getInstance(Key.get(Integer.class, Names.named("GrassTileTexture"))));
        grass.setIsSelectedTexture(textureFactory.loadTexture(R.drawable.overlays_selected));
        return grass;
    }
    
    /** Legal tile types. */
    public enum Type {
        GRASS
    }
}
