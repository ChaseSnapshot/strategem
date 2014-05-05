package com.sneaky.stratagem.armyselection;

import android.opengl.GLES20;

import com.sneaky.stratagem.R;
import com.sneaky.stratagem.StratagemModule;
import com.sneaky.stratagem.actions.AttackAction.AttackActionFactory;
import com.sneaky.stratagem.actions.BashAction.BashActionFactory;
import com.sneaky.stratagem.actions.BuildWallAction.BuildWallActionFactory;
import com.sneaky.stratagem.actions.ChargeAction.ChargeActionFactory;
import com.sneaky.stratagem.actions.EarthSplitterAction.EarthSplitterActionFactory;
import com.sneaky.stratagem.actions.ManaBatteryAction.ManaBatteryActionFactory;
import com.sneaky.stratagem.actions.MoveAction.MoveActionFactory;
import com.sneaky.stratagem.actions.RaiseHand.RaiseHandFactory;
import com.sneaky.stratagem.actions.StunAction.StunActionFactory;
import com.sneaky.stratagem.actions.TeleportAction.TeleportActionFactory;
import com.sneaky.stratagem.actions.WarCry.WarCryFactory;
import com.sneaky.stratagem.armyselection.AvailableUnitEntry.AvailableUnitEntryFactory;
import com.sneaky.stratagem.armyselection.EmptyEntry.EmptyEntryFactory;
import com.sneaky.stratagem.armyselection.EnlistedUnitEntry.EnlistedUnitEntryFactory;
import com.sneaky.stratagem.graphics.textures.TextureFactory;
import com.sneaky.stratagem.graphics.textures.UnitTexturePack.UnitTexturePackFactory;
import com.sneaky.stratagem.graphics.widgets.Button.ButtonFactory;
import com.sneaky.stratagem.graphics.widgets.MessageBox.MessageBoxFactory;
import com.sneaky.stratagem.proxy.ProxyRenderer;
import com.sneaky.stratagem.units.commanders.MeteorStrike;

import scatcat.general.Constants;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;

public class ArmySelectionModule extends StratagemModule {
    public ArmySelectionModule(final ProxyRenderer renderer) {
        super(renderer);
    }
    
    public void close(final Injector injector) {
        getView().queueEvent(new Runnable() {
           @Override
           public void run() {
               final int   numTextures = 8;
               final int[] toBeDeleted = new int[numTextures];
               
               toBeDeleted[0] = injector.getInstance(Key.get(Integer.class, Names.named("DetailedUnitViewBackgroundTexture")));
               toBeDeleted[1] = injector.getInstance(Key.get(Integer.class, Names.named("EmptyEntryBackgroundTexture")));
               toBeDeleted[2] = injector.getInstance(Key.get(Integer.class, Names.named("UnitEntryBackgroundTexture")));
               toBeDeleted[3] = injector.getInstance(Key.get(Integer.class, Names.named("EntryOrbTexture")));
               toBeDeleted[4] = injector.getInstance(Key.get(Integer.class, Names.named("GlyphMapTexture")));
               toBeDeleted[5] = injector.getInstance(Key.get(Integer.class, Names.named("RecruitPointsOrbTexture")));
               toBeDeleted[6] = injector.getInstance(Key.get(Integer.class, Names.named("SimpleArrowTexture")));
               toBeDeleted[7] = injector.getInstance(Key.get(Integer.class, Names.named("SimpleButtonTexture")));
               
               GLES20.glDeleteTextures(numTextures, toBeDeleted, Constants.NO_OFFSET);
           }
        });
    }
    
    @Override
    protected void configure() {
        super.configure();
        
        installFactories();
        
        bind(ArmySelectionRenderer.class).in(Singleton.class);
        bind(ArmySelectionModule.class).toInstance(this);
        bind(Integer.class).annotatedWith(Names.named("GlyphInfo")).toInstance(R.raw.patrickhandsc);
        bind(TextureFactory.class).in(Singleton.class);
    }
    
    private void installFactories() {
        install(new FactoryModuleBuilder().build(AvailableUnitEntryFactory.class));
        install(new FactoryModuleBuilder().build(ButtonFactory.class));
        install(new FactoryModuleBuilder().build(EmptyEntryFactory.class));
        install(new FactoryModuleBuilder().build(EnlistedUnitEntryFactory.class));
        install(new FactoryModuleBuilder().build(MessageBoxFactory.class));
        install(new FactoryModuleBuilder().build(UnitTexturePackFactory.class));
        
        // Nullify the battle specific dependencies of the units
        bind(AttackActionFactory.class).toProvider(Providers.<AttackActionFactory>of(null));
        bind(BashActionFactory.class).toProvider(Providers.<BashActionFactory>of(null));
        bind(BuildWallActionFactory.class).toProvider(Providers.<BuildWallActionFactory>of(null));
        bind(ChargeActionFactory.class).toProvider(Providers.<ChargeActionFactory>of(null));
        bind(EarthSplitterActionFactory.class).toProvider(Providers.<EarthSplitterActionFactory>of(null));
        bind(ManaBatteryActionFactory.class).toProvider(Providers.<ManaBatteryActionFactory>of(null));
        bind(MeteorStrike.class).toProvider(Providers.<MeteorStrike>of(null));
        bind(MoveActionFactory.class).toProvider(Providers.<MoveActionFactory>of(null));
        bind(RaiseHandFactory.class).toProvider(Providers.<RaiseHandFactory>of(null));
        bind(StunActionFactory.class).toProvider(Providers.<StunActionFactory>of(null));
        bind(TeleportActionFactory.class).toProvider(Providers.<TeleportActionFactory>of(null));
        bind(WarCryFactory.class).toProvider(Providers.<WarCryFactory>of(null));
    }
    
    @Provides @Singleton @Named("DetailedUnitViewBackgroundTexture")
    private int provideDetailedUnitViewBackgroundTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.army_selection_entry_background); //TODO
    }
    
    @Provides @Singleton @Named("EmptyEntryBackgroundTexture")
    private int provideEmptyEntryBackgroundTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.army_selection_empty_entry_background);
    }
    
    @Provides @Singleton @Named("UnitEntryBackgroundTexture")
    private int provideEntryBackgroundTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.army_selection_entry_background);
    }
    
    @Provides @Singleton @Named("EntryOrbTexture")
    private int provideEntryOrbTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.army_selection_entry_orb);
    }
    
    @Provides @Singleton @Named("GlyphMapTexture")
    private int provideGlyphMapTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.glyph_map_patrick_hand_sc);
    }
    
    @Provides @Singleton @Named("RecruitPointsOrbTexture")
    private int provideRecruitPointsOrbTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.army_selection_recruit_points_orb);
    }
    
    @Provides @Singleton @Named("SimpleArrowTexture")
    private int provideSimpleArrowTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.simple_arrow);
    }

    @Provides @Named("SimpleButtonTexture") @Singleton
    private int provideSimpleButtonTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.simple_button);
    }
}
