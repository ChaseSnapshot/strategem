package scatcat.map;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Factory class that abstracts away the details of constructing different types
 * of battlefields.
 * 
 * @author R. Matt McCann
 */
public final class BattlefieldFactory {
    private final Injector injector;
    
    private final TileFactory tileFactory;
    
    @Inject
    protected BattlefieldFactory(final Injector injector,
                                 final TileFactory tileFactory) { 
        this.injector = injector;
        this.tileFactory = tileFactory;
    }

    /**
     * Primary construction function. Takes a battlefield type argument
     * and constructs the request battlefield.
     * 
     * @param type Type of battlefield to create.
     * 
     * @return Constructed battlefield
     */
    public Battlefield create(Type type) {
        switch(type) {
            case PROTOTYPE:
                return createPrototype();
            default:
                System.err.println("Unknown battlefield type!");
                return injector.getInstance(Battlefield.class);
        }
    };
    
    /**
     * Creates a simple test battlefield.
     * 
     * @return Test battlefield
     */
    public Battlefield createPrototype() {
        Battlefield prototype = injector.getInstance(Battlefield.class);
        //prototype.setIslandBase(ModelHelper.buildModel(ModelHelper.Type.ISLAND));
        //prototype.setBackground(BackgroundFactory.create(BackgroundFactory.WeatherType.FAIR_WEATHER));
 
        for (int rowIter = 0; rowIter < 10; rowIter++) {
            for (int colIter = 0; colIter < 10; colIter++) {
                prototype.addTile(rowIter, colIter, tileFactory.create(TileFactory.Type.GRASS));
            }
        }

        return prototype;
    }
    
    /** Types of battlefields that can be constructed. */
    public enum Type {
        PROTOTYPE
    }
}
