package com.sneaky.stratagem.map.background;

import java.util.Random;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.sneaky.stratagem.R;
import com.sneaky.stratagem.graphics.textures.TextureFactory;

import scatcat.general.points.Point2D;
import scatcat.general.points.Point3D;

public class CloudFactory {
    private final Injector injector;
    
    private final TextureFactory textureFactory;
    
    @Inject
    protected CloudFactory(final Injector injector,
                           final TextureFactory textureFactory) {
        this.injector = injector;
        this.textureFactory = textureFactory;
    }
    
    public Cloud createRandomCloud() {
        Cloud cloud = injector.getInstance(Cloud.class);
        
        // Generate a random position
        Random randomizer = new Random();
        Point3D position = new Point3D();
        position.setX(0.5f - randomizer.nextFloat());
        position.setY(0.5f - randomizer.nextFloat());
        position.setZ(-1.0f * randomizer.nextFloat() - 1.0f);
        cloud.setPosition(position);
        
        // Generate a lightly variable size
        Point2D size = new Point2D();
        size.setX(0.5f - 0.025f * randomizer.nextFloat());
        size.setY(0.25f - 0.0125f * randomizer.nextFloat());
        cloud.setSize(size);
        
        // Generate a random cloud texture //TODO
        cloud.setTexture(textureFactory.loadTexture(R.drawable.background_feature_cloud1));
        
        // Generate a random float direction and speed
        cloud.setFloatSpeed(0.02f + 0.08f * randomizer.nextFloat());
        
        return cloud;
    }
}
