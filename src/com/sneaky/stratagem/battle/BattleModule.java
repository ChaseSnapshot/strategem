package com.sneaky.stratagem.battle;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.opengl.GLES20;

import com.sneaky.stratagem.R;
import com.sneaky.stratagem.StratagemModule;
import com.sneaky.stratagem.actions.AttackAction.AttackActionFactory;
import com.sneaky.stratagem.actions.BashAction.BashActionFactory;
import com.sneaky.stratagem.actions.BuildWallAction.BuildWallActionFactory;
import com.sneaky.stratagem.actions.ChargeAction.ChargeActionFactory;
import com.sneaky.stratagem.actions.EarthSpike.EarthSpikeFactory;
import com.sneaky.stratagem.actions.EarthSplitterAction.EarthSplitterActionFactory;
import com.sneaky.stratagem.actions.HelpingHand.HelpingHandFactory;
import com.sneaky.stratagem.actions.ManaBatteryAction.ManaBatteryActionFactory;
import com.sneaky.stratagem.actions.MoveAction.MoveActionFactory;
import com.sneaky.stratagem.actions.RaiseHand.RaiseHandFactory;
import com.sneaky.stratagem.actions.StunAction.StunActionFactory;
import com.sneaky.stratagem.actions.TeleportAction.TeleportActionFactory;
import com.sneaky.stratagem.actions.WarCry.WarCryFactory;
import com.sneaky.stratagem.actions.threats.LinearAttackPattern.LinearAttackPatternFactory;
import com.sneaky.stratagem.actions.threats.LinearChargePattern.LinearChargePatternFactory;
import com.sneaky.stratagem.actions.threats.RadialAttackPattern.RadialAttackPatternFactory;
import com.sneaky.stratagem.actions.threats.RadialBuffPattern.RadialBuffPatternFactory;
import com.sneaky.stratagem.actions.threats.RadialMovementPattern.RadialMovementPatternFactory;
import com.sneaky.stratagem.actions.threats.RadialPassivePattern.RadialPassivePatternFactory;
import com.sneaky.stratagem.effects.MoveBuff.MoveBuffFactory;
import com.sneaky.stratagem.effects.StunEffect.StunEffectFactory;
import com.sneaky.stratagem.graphics.textures.HighlightTexturePack;
import com.sneaky.stratagem.graphics.textures.SingleTexturePack.SingleTexturePackFactory;
import com.sneaky.stratagem.graphics.textures.TextureFactory;
import com.sneaky.stratagem.graphics.textures.UnitTexturePack.UnitTexturePackFactory;
import com.sneaky.stratagem.graphics.widgets.Button.ButtonFactory;
import com.sneaky.stratagem.graphics.widgets.StatsBar.StatsBarFactory;
import com.sneaky.stratagem.graphics.widgets.actionmenu.ActionMenuItem.ActionMenuItemFactory;
import com.sneaky.stratagem.map.background.BackgroundFactory;
import com.sneaky.stratagem.proxy.ProxyRenderer;
import com.sneaky.stratagem.units.commanders.MeteorStrike.MeteorStrikeFactory;

import scatcat.effects.StatusMessageAnimation.StatusMessageAnimationFactory;
import scatcat.general.Constants;
import scatcat.map.Battlefield;

import com.google.common.eventbus.EventBus;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Dependency injection configuration for the game client.
 * 
 * @author R. Matt McCann
 */
public class BattleModule extends StratagemModule {
    //private final Match match;
    
    private final ProxyRenderer renderer;
    
    private final Map<String, Integer> textures = new HashMap<String, Integer>();
    
    public BattleModule(final ProxyRenderer renderer) {
        super(renderer);
        
        //this.match = match;
        this.renderer = renderer;
    }
    
    public void close(final Injector injector) {
        getView().queueEvent(new Runnable() {
           @Override
           public void run() {
               final int   numTextures = 20;
               final int[] toBeDeleted = new int[numTextures];
               
               toBeDeleted[0]  = injector.getInstance(Key.get(Integer.class, Names.named("AbilityPowerTexture")));
               toBeDeleted[1]  = injector.getInstance(Key.get(Integer.class, Names.named("ActionMenuAttackIcon")));
               toBeDeleted[2]  = injector.getInstance(Key.get(Integer.class, Names.named("ActionMenuBottom")));
               toBeDeleted[3]  = injector.getInstance(Key.get(Integer.class, Names.named("ActionMenuGradient")));
               toBeDeleted[4]  = injector.getInstance(Key.get(Integer.class, Names.named("ActionMenuTop")));
               toBeDeleted[5]  = injector.getInstance(Key.get(Integer.class, Names.named("ActionMenuMoveIcon")));
               toBeDeleted[6]  = injector.getInstance(Key.get(Integer.class, Names.named("BackgroundOrbTexture")));
               toBeDeleted[7]  = injector.getInstance(Key.get(Integer.class, Names.named("genericGrayMenuItem")));
               toBeDeleted[8]  = injector.getInstance(Key.get(Integer.class, Names.named("GlyphMapTexture")));
               toBeDeleted[9]  = injector.getInstance(Key.get(Integer.class, Names.named("HeartTexture")));
               toBeDeleted[10] = injector.getInstance(Key.get(Integer.class, Names.named("GrassTileTexture")));
               toBeDeleted[12] = injector.getInstance(Key.get(Integer.class, Names.named("MainMenuTop")));
               toBeDeleted[13] = injector.getInstance(Key.get(Integer.class, Names.named("MenuGearTexture")));
               toBeDeleted[14] = injector.getInstance(Key.get(Integer.class, Names.named("MenuItemBackground")));
               toBeDeleted[15] = injector.getInstance(Key.get(Integer.class, Names.named("MenuItemBackgroundSelected")));
               toBeDeleted[16] = injector.getInstance(Key.get(Integer.class, Names.named("MenuItemUnselectable")));
               toBeDeleted[17] = injector.getInstance(Key.get(Integer.class, Names.named("RedTeamOverlay")));
               toBeDeleted[18] = injector.getInstance(Key.get(Integer.class, Names.named("SimpleArrowTexture")));
               toBeDeleted[19] = injector.getInstance(Key.get(Integer.class, Names.named("unselectableGrayMenuItem")));
               
               GLES20.glDeleteTextures(numTextures, toBeDeleted, Constants.NO_OFFSET);
               
               injector.getInstance(Key.get(HighlightTexturePack.class, Names.named("Attack"))).cleanUp();
           }
        });
    }
    
    /** Configures the dependency graph. */
    @Override
    protected void configure() {
        super.configure();
        
        bind(BackgroundFactory.class).in(Singleton.class);
        bind(Battlefield.class).in(Singleton.class);
        bind(BattleModule.class).toInstance(this);
        bind(BattleRenderer.class).in(Singleton.class);
        bind(EventBus.class).in(Singleton.class);
        bind(Integer.class).annotatedWith(Names.named("GlyphInfo")).toInstance(R.raw.patrickhandsc);
        
        bind(TextureFactory.class).in(Singleton.class);
        
        bindAndLoadTextures();
        installFactories();
    }
    
    private void bindAndLoadTextures() {
        textures.put("ActionMenuItemBackground", R.drawable.menu_action_background);
        textures.put("ActionMenuItemUnselectable", R.drawable.menu_item_unselectable);
        textures.put("AttackIcon", R.drawable.menu_action_icon_attack);
        textures.put("EarthSpikeTexture", R.drawable.earth_splitter_spike);
        textures.put("HelpingHandTexture", R.drawable.helping_hand);
        textures.put("MoveIcon", R.drawable.menu_action_icon_move);
        textures.put("SimpleButtonTexture", R.drawable.simple_button);
        
        
        TextureFactory textureFactory = new TextureFactory(renderer.getContext());
        for (Entry<String, Integer> entry : textures.entrySet()) {
            bind(Key.get(Integer.class, Names.named(entry.getKey())))
                .toInstance(textureFactory.loadTexture(entry.getValue()));
        }
    }
    
    private void installFactories() {
        install(new FactoryModuleBuilder().build(ActionMenuItemFactory.class));
        install(new FactoryModuleBuilder().build(AttackActionFactory.class));
        install(new FactoryModuleBuilder().build(BashActionFactory.class));
        install(new FactoryModuleBuilder().build(BuildWallActionFactory.class));
        install(new FactoryModuleBuilder().build(ButtonFactory.class));
        install(new FactoryModuleBuilder().build(ChargeActionFactory.class));
        install(new FactoryModuleBuilder().build(EarthSpikeFactory.class));
        install(new FactoryModuleBuilder().build(EarthSplitterActionFactory.class));
        install(new FactoryModuleBuilder().build(HelpingHandFactory.class));
        install(new FactoryModuleBuilder().build(LinearAttackPatternFactory.class));
        install(new FactoryModuleBuilder().build(LinearChargePatternFactory.class));
        install(new FactoryModuleBuilder().build(MeteorStrikeFactory.class));
        install(new FactoryModuleBuilder().build(ManaBatteryActionFactory.class));
        install(new FactoryModuleBuilder().build(MoveActionFactory.class));
        install(new FactoryModuleBuilder().build(MoveBuffFactory.class));
        install(new FactoryModuleBuilder().build(RadialAttackPatternFactory.class));
        install(new FactoryModuleBuilder().build(RadialBuffPatternFactory.class));
        install(new FactoryModuleBuilder().build(RadialMovementPatternFactory.class));
        install(new FactoryModuleBuilder().build(RadialPassivePatternFactory.class));
        install(new FactoryModuleBuilder().build(RaiseHandFactory.class));
        install(new FactoryModuleBuilder().build(SingleTexturePackFactory.class));
        install(new FactoryModuleBuilder().build(StatsBarFactory.class));
        install(new FactoryModuleBuilder().build(StunActionFactory.class));
        install(new FactoryModuleBuilder().build(StunEffectFactory.class));
        install(new FactoryModuleBuilder().build(StatusMessageAnimationFactory.class));
        install(new FactoryModuleBuilder().build(TeleportActionFactory.class));
        install(new FactoryModuleBuilder().build(UnitTexturePackFactory.class));
        install(new FactoryModuleBuilder().build(WarCryFactory.class));
    }
    
    @Provides @Singleton @Named("AbilityPowerTexture")
    private final int provideAbilityPowerTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.ability_power);
    }
    
    @Provides @Singleton @Named("ActionMenuAttackIcon")
    private Integer provideActionMenuAttackIcon(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_action_icon_attack);
    }
    
    @Provides @Singleton @Named("ActionMenuBottom")
    private Integer provideActionMenuBottom(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_action_bottom);
    }
    
    @Provides @Singleton @Named("ActionMenuTop")
    private Integer provideActionMenuTop(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_action_top);
    }
    
    @Provides @Singleton @Named("ActionMenuMoveIcon")
    private Integer provideActionMenuMoveIcon(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_action_icon_move);
    }
    
    @Provides @Singleton @Named("Attack")
    private HighlightTexturePack provideAttackHighlightTexturePack(final Injector injector,
                                                                   final TextureFactory textureFactory) {
        return new HighlightTexturePack(injector,
                textureFactory.loadTexture(R.drawable.overlays_attack_bridge),
                textureFactory.loadTexture(R.drawable.overlays_attack_center),
                textureFactory.loadTexture(R.drawable.overlays_attack_corner),
                textureFactory.loadTexture(R.drawable.overlays_attack_corner_missing_1_corner),
                textureFactory.loadTexture(R.drawable.overlays_attack_island),
                textureFactory.loadTexture(R.drawable.overlays_attack_missing_1_corner),
                textureFactory.loadTexture(R.drawable.overlays_attack_missing_2_corners),
                textureFactory.loadTexture(R.drawable.overlays_attack_missing_2_corners_opposite),
                textureFactory.loadTexture(R.drawable.overlays_attack_missing_3_corners),
                textureFactory.loadTexture(R.drawable.overlays_attack_missing_4_corners),
                textureFactory.loadTexture(R.drawable.overlays_attack_peninsula),
                textureFactory.loadTexture(R.drawable.overlays_attack_shore),
                textureFactory.loadTexture(R.drawable.overlays_attack_shore_missing_1_corner),
                textureFactory.loadTexture(R.drawable.overlays_attack_shore_missing_2_corners));
    }
    
    @Provides @Singleton @Named("BuffAction")
    private HighlightTexturePack provideBuffHighlightTexturePack(final Injector injector,
                                                                 final TextureFactory textureFactory) {
        return provideMoveHighlightTexturePack(injector, textureFactory);
    }
    
    @Provides @Singleton @Named("Move")
    private HighlightTexturePack provideMoveHighlightTexturePack(final Injector injector,
                                                                 final TextureFactory textureFactory) {
        return new HighlightTexturePack(injector,
                textureFactory.loadTexture(R.drawable.overlays_move_bridge),
                textureFactory.loadTexture(R.drawable.overlays_move_center),
                textureFactory.loadTexture(R.drawable.overlays_move_corner),
                textureFactory.loadTexture(R.drawable.overlays_move_corner_missing_1_corner),
                textureFactory.loadTexture(R.drawable.overlays_move_island),
                textureFactory.loadTexture(R.drawable.overlays_move_missing_1_corner),
                textureFactory.loadTexture(R.drawable.overlays_move_missing_2_corners),
                textureFactory.loadTexture(R.drawable.overlays_move_missing_2_corners_opposite),
                textureFactory.loadTexture(R.drawable.overlays_move_missing_3_corners),
                textureFactory.loadTexture(R.drawable.overlays_move_missing_4_corners),
                textureFactory.loadTexture(R.drawable.overlays_move_peninsula),
                textureFactory.loadTexture(R.drawable.overlays_move_shore),
                textureFactory.loadTexture(R.drawable.overlays_move_shore_missing_1_corner),
                textureFactory.loadTexture(R.drawable.overlays_move_shore_missing_2_corners));
    }
    
    @Provides @Singleton @Named("PassiveAction")
    private HighlightTexturePack providePassiveActionHighlights(final Injector injector,
                                                                final TextureFactory textureFactory) {
        return provideAttackHighlightTexturePack(injector, textureFactory);
    }
    
    @Provides @Singleton @Named("BackgroundOrbTexture")
    private final int provideBackgroundOrbTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.army_selection_entry_orb);
    }
    
    @Provides @Singleton @Named("genericGrayMenuItem")
    private final int provideGenericGrayMenuItem(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_main_item);
    }
    
    @Provides @Singleton @Named("GlyphMapTexture")
    private int provideGlyphMapTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.glyph_map_patrick_hand_sc);
    }
    
    @Provides @Singleton @Named("HeartTexture")
    private int provideHeartTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.heart);
    }
    
    @Provides @Singleton @Named("GrassTileTexture")
    private int provideGrassTileTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.grass);
    }
    
    @Provides @Singleton @Named("MainMenuTop")
    private int provideMainMenuTopTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_main_top);
    }
    
    @Provides @Singleton @Named("MenuGearTexture")
    private int provideMenuGearTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_gear);
    }
    
    @Provides @Singleton @Named("MenuItemBackground")
    private int provideMenuItemBackground(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_item_background);
    }
    
    @Provides @Singleton @Named("MenuItemBackgroundSelected")
    private int provideMenuItemBackgroundSelected(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_item_background_selected);
    }
    
    @Provides @Singleton @Named("MenuItemUnselectable")
    private int provideMenuItemUnselectable(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_item_unselectable);
    }
    
    @Provides @Singleton @Named("BlueTeamOverlay")
    private final int provideBlueTeamOverlay(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.overlays_team_blue);
    }
    
    @Provides @Singleton @Named("RedTeamOverlay")
    private final int provideRedTeamOverlay(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.overlays_team_red);
    }
    
    @Provides @Singleton @Named("SimpleArrowTexture")
    private int provideSimpleArrowTexture(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.simple_arrow);
    }
    
    @Provides @Singleton @Named("unselectableGrayMenuItem")
    private final int provideUnselectableGrayMenuItem(final TextureFactory textureFactory) {
        return textureFactory.loadTexture(R.drawable.menu_item_unselectable);
    }
}
