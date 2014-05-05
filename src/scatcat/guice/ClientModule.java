package scatcat.guice;

import com.sneaky.stratagem.R;
import scatcat.general.Device;
import scatcat.map.Battlefield;
import scatcat.map.BattlefieldFactory;
import android.content.Context;
import android.opengl.GLSurfaceView;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.sneaky.stratagem.flow.GameFlowController;
import com.sneaky.stratagem.flow.GameFlowControllerImpl;
import com.sneaky.stratagem.graphics.textures.TextureFactory;
import com.sneaky.stratagem.match.LocalMatch;
import com.sneaky.stratagem.match.Match;

/**
 * Dependency injection configuration for the game client.
 * 
 * @author R. Matt McCann
 */
public class ClientModule extends AbstractModule {
    /** Temporary holding place for the application's context. */
    private final Context context;
    
    public ClientModule(final Context context) {
        this.context = context;
    }
    
    /** Configures the dependency graph. */
    @Override
    protected void configure() {
        bind(Context.class)
                .toInstance(context);
        bind(GameFlowController.class)
                .to(GameFlowControllerImpl.class);
        bind(Match.class)
                .to(LocalMatch.class);
    }
    
    @Provides @Singleton
    private final Battlefield provideBattlefield(final BattlefieldFactory factory) {
        return factory.create(BattlefieldFactory.Type.PROTOTYPE);
    }
    
    @Provides @Singleton @Named("genericGrayMenuItem")
    private final int provideGenericGrayMenuItem(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_main_item);
    }
    
    @Provides @Singleton @Named("unselectableGrayMenuItem")
    private final int provideUnselectableGrayMenuItem(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_item_unselectable);
    }
}
