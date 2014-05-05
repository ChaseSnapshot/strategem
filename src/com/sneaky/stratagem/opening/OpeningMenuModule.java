package com.sneaky.stratagem.opening;

import scatcat.general.Constants;
import android.opengl.GLES20;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.sneaky.stratagem.R;
import com.sneaky.stratagem.StratagemModule;
import com.sneaky.stratagem.graphics.textures.TextureFactory;
import com.sneaky.stratagem.graphics.widgets.Button.ButtonFactory;
import com.sneaky.stratagem.proxy.ProxyRenderer;

public class OpeningMenuModule extends StratagemModule {
    public OpeningMenuModule(final ProxyRenderer renderer) {
        super(renderer);
    }
    
    /** Release all OpenGL resources allocated by this module. */
    public void close(final Injector injector) {
        getView().queueEvent(new Runnable() {
            @Override
            public void run() {
                final int numTextures = 3;
                final int[] toBeDeleted = new int[numTextures];
                toBeDeleted[0] = injector.getInstance(Key.get(Integer.class, Names.named("ButtonBackgroundTexture")));
                toBeDeleted[1] = injector.getInstance(Key.get(Integer.class, Names.named("GlyphMapTexture")));
                toBeDeleted[2] = injector.getInstance(Key.get(Integer.class, Names.named("SplashTexture")));
                GLES20.glDeleteTextures(numTextures, toBeDeleted, Constants.NO_OFFSET);
            }
        });
    }
    
    @Override
    protected void configure() {
        super.configure();
        
        bind(Integer.class).annotatedWith(Names.named("GlyphInfo")).toInstance(R.raw.patrickhandsc);
        bind(OpeningMenuModule.class).toInstance(this);
        bind(TextureFactory.class).in(Singleton.class);
        
        install(new FactoryModuleBuilder().build(ButtonFactory.class));
    }
    
    @Provides @Singleton @Named("ButtonBackgroundTexture")
    private Integer provideButtonBackgroundTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.simple_button);
    }
    
    @Provides @Singleton @Named("GlyphMapTexture")
    private int provideGlyphMapTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.glyph_map_patrick_hand_sc);
    }

    @Provides @Singleton @Named("SplashTexture")
    private int provideSplashTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.title_splash);
    }
}
