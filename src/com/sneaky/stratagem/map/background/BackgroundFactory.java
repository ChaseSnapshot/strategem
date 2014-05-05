package com.sneaky.stratagem.map.background;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Factory class for the production of background objects. Responsibilities
 * include instantiating background objects and tracking/singleton'ing background
 * textures.
 * 
 * @author R. Matt McCann
 */
public class BackgroundFactory {
    private final Injector injector;
    
    /** Enumeration of valid feature types */
    public enum FeatureType {
        /** A simple cloud */
        CLOUD
    }
    
    /** Enumeration of valid background types */
    public enum WeatherType {
        /** A simple floating clouds scene */
        FAIR_WEATHER
    };
    
	/** Guice injection constructor. */
	@Inject
    protected BackgroundFactory(final Injector injector) {
	    this.injector = injector;
	}
	
	/**
	 * Constructs background objects of the specified type.
	 * 
	 * @param type	Type of background to construct
	 * @return		Constructed background object
	 */
	public Background create(final WeatherType type) {
		switch(type) {
			case FAIR_WEATHER:
				return injector.getInstance(FairWeather.class);
			default:
				System.err.println("Unhandled background type!");
				return null;
		}
	}
}
