package scatcat.guice;

import com.sneaky.stratagem.R;
import com.sneaky.stratagem.graphics.textures.TextureFactory;


import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

public class TexturesModule extends AbstractModule {

    @Override
    protected void configure() { }

    @Provides @Singleton @Named("ActionMenuAttackIcon")
    private Integer provideActionMenuAttackIcon(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_action_icon_attack);
    }
    
    @Provides @Singleton @Named("ActionMenuBottom")
    private Integer provideActionMenuBottom(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_action_bottom);
    }

    @Provides @Singleton @Named("ActionMenuGradient")
    private Integer provideActionMenuGradient(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_action_background);
    }
    
    @Provides @Singleton @Named("ActionMenuTop")
    private Integer provideActionMenuTop(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_action_top);
    }
    
    @Provides @Singleton @Named("ActionMenuMoveIcon")
    private Integer provideActionMenuMoveIcon(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_action_icon_move);
    }
}
