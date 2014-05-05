package com.sneaky.stratagem.units;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import scatcat.general.Constants;
import android.opengl.GLES20;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.sneaky.stratagem.R;
import com.sneaky.stratagem.graphics.textures.TextureFactory;
import com.sneaky.stratagem.proxy.ProxyRenderer;

public class UnitModule extends AbstractModule {
    private final ProxyRenderer renderer;
    
    private final Map<String, Integer> textures = new HashMap<String, Integer>();
    
    public UnitModule(ProxyRenderer renderer) {
        this.renderer = renderer;
    }

    public void close(final Injector injector) {
        renderer.getView().queueEvent(new Runnable() {
           @Override
           public void run() {
               final int   numTextures = 7;
               final int[] toBeDeleted = new int[numTextures];
               
               toBeDeleted[0] = injector.getInstance(Key.get(Integer.class, Names.named("ExplosionTexture")));
               toBeDeleted[1] = injector.getInstance(Key.get(Integer.class, Names.named("MeteorTexture")));
               //TODO
               
               GLES20.glDeleteTextures(numTextures, toBeDeleted, Constants.NO_OFFSET);
           }
        });
    }
    
    @Override
    protected void configure() { 
        bind(UnitModule.class).toInstance(this);
        
        bindAndLoadTextures();
    }
    
    private void bindAndLoadTextures() {
        textures.put("AdeptBack", R.drawable.adept_back);
        textures.put("AdeptFront", R.drawable.adept_front);
        textures.put("AmplifierBack", R.drawable.amplifier_back);
        textures.put("AmplifierFront", R.drawable.amplifier_front);
        textures.put("ArmorBack", R.drawable.armor_back);
        textures.put("ArmorFront", R.drawable.armor_front);
        textures.put("CrazyHomonculusBack", R.drawable.crazy_homonculus_back);
        textures.put("CrazyHomonculusFront", R.drawable.crazy_homonculus_front);
        textures.put("ElementalBack", R.drawable.elemental_back);
        textures.put("ElementalFront", R.drawable.elemental_front);
        textures.put("HandMageBack", R.drawable.handmage_back);
        textures.put("HandMageFront", R.drawable.handmage_front);
        textures.put("HomonculusBack", R.drawable.homonculus_back);
        textures.put("HomonculusFront", R.drawable.homonculus_front);
        textures.put("MasonsWall", R.drawable.mason_wall);
        textures.put("SageBack", R.drawable.sage_back);
        textures.put("SageFront", R.drawable.sage_front);
        
        TextureFactory textureFactory = new TextureFactory(renderer.getContext());
        for (Entry<String, Integer> entry : textures.entrySet()) {
            bind(Key.get(Integer.class, Names.named(entry.getKey())))
                .toInstance(textureFactory.loadTexture(entry.getValue()));
        }
    }

    @Provides @Singleton @Named("BlueWizardEast")
    private Integer provideBlueWizardEast(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.blue_wizard_east);
    }
    
    @Provides @Singleton @Named("BlueWizardNorth")
    private Integer provideBlueWizardNorth(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.blue_wizard_north);
    }
    
    @Provides @Singleton @Named("BlueWizardProfile")
    private Integer provideBlueWizardProfile(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.blue_wizard_east);
    }
    
    @Provides @Singleton @Named("BlueWizardSouth")
    private Integer provideBlueWizardSouth(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.blue_wizard_south);
    }
    
    @Provides @Singleton @Named("BlueWizardWest")
    private Integer provideBlueWizardWest(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.blue_wizard_west);
    }
    
    @Provides @Singleton @Named("RedKnightEast")
    private Integer provideRedKnightEast(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.commander_red_knight_east);
    }
    
    @Provides @Singleton @Named("RedKnightNorth")
    private Integer provideRedKnightNorth(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.commander_red_knight_north);
    }
    
    @Provides @Singleton @Named("RedKnightProfile")
    private Integer provideRedKnightProfile(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.commander_red_knight_east);
    }
    
    @Provides @Singleton @Named("RedKnightSouth")
    private Integer provideRedKnightSouth(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.commander_red_knight_south);
    }
    
    @Provides @Singleton @Named("RedKnightWest")
    private Integer provideRedKnightWest(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.commander_red_knight_west);
    }
    
    @Provides @Singleton @Named("BruteBack")
    private Integer provideBruteBack(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.brute_back);
    }
    
    @Provides @Singleton @Named("BruteFront")
    private Integer provideBruteFront(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.brute_front);
    }
     
    @Provides @Singleton @Named("CatapultBack")
    private Integer provideCatapultBack(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.catapult_back);
    }
    
    @Provides @Singleton @Named("CatapultFront")
    private Integer provideCatapultFront(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.catapult_front);
    }
    
    @Provides @Singleton @Named("CrossbowmanBack")
    private Integer provideCrossbowmanBack(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.crossbowman_back);
    }
    
    @Provides @Singleton @Named("CrossbowmanFront")
    private Integer provideCrossbowmanFront(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.crossbowman_front);
    }
    
    @Provides @Singleton @Named("DrummerBack")
    private Integer provideDrummerBack(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.drummer_back);
    }
    
    @Provides @Singleton @Named("DrummerFront")
    private Integer provideDrummerFront(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.drummer_front);
    }
    
    @Provides @Singleton @Named("EngieBack")
    private Integer provideEngieBack(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.engie_back);
    }
    
    @Provides @Singleton @Named("EngieFront")
    private Integer provideEngieFront(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.engie_front);
    }
    
    @Provides @Singleton @Named("MasonBack")
    private Integer provideMasonBack(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.mason_back);
    }
    
    @Provides @Singleton @Named("MasonFront")
    private Integer provideMasonFront(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.mason_front);
    }
    
    @Provides @Singleton @Named("ShieldBearerBack")
    private Integer provideShieldbearerBack(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.shieldbearer_back);
    }
    
    @Provides @Singleton @Named("ShieldBearerFront")
    private Integer provideShieldbearerFront(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.shieldbearer_front);
    }
    
    @Provides @Singleton @Named("SquireBack")
    private Integer provideSquireBack(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.squire_back);
    }
    
    @Provides @Singleton @Named("SquireFront")
    private Integer provideSquireFront(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.squire_front);
    }
    
    @Provides @Singleton @Named("ExplosionTexture")
    private Integer provideExplosionTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.blue_wizard_meteor_strike_explosion);
    }
    
    @Provides @Singleton @Named("MeteorTexture")
    private Integer provideMeteorTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.blue_wizard_meteor_strike_meteorite);
    }
}