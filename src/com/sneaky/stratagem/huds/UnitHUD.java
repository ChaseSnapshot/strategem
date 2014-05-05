package com.sneaky.stratagem.huds;

import scatcat.exceptions.UnprojectionException;
import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.points.GridPoint2D;
import scatcat.general.points.NormalizedPoint2D;
import scatcat.general.points.Point2D;
import scatcat.graphics.Color;
import scatcat.graphics.DrawUtils;
import scatcat.graphics.MVP;
import scatcat.graphics.shaders.SimpleColorShader;
import scatcat.graphics.shaders.SimpleTexturedShader;
import scatcat.input.InputHelper;
import scatcat.map.Battlefield;
import scatcat.map.Tile;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sneaky.stratagem.battle.BattleRenderer;
import com.sneaky.stratagem.graphics.textures.TextureFactory;
import com.sneaky.stratagem.graphics.widgets.StatsBar;
import com.sneaky.stratagem.graphics.widgets.StatsBar.StatsBarFactory;
import com.sneaky.stratagem.graphics.widgets.actionmenu.ActionMenu;
import com.sneaky.stratagem.graphics.widgets.menus.ExpandingMenu;
import com.sneaky.stratagem.graphics.widgets.menus.Menu;
import com.sneaky.stratagem.graphics.widgets.menus.MenuFactory;
import com.sneaky.stratagem.units.Unit;

/**
 * Provides a Heads Up Display for units when they
 * are selected on the battlefield.
 * 
 * @author R. Matt McCann
 */
@Singleton
public class UnitHUD extends HUD {
    /** Menu used to display unit action options. */
    private final ActionMenu actionMenu;
    
    private final Device device;
    
    private final float gemHeight = 0.3f;
    private final float gemWidth;
    
    /** Used for avoiding circular dependencies. */
    private final Injector injector;
    
    private final SimpleColorShader colorShader;
    private final SimpleTexturedShader texturedShader;
    
    /** Used for generating text. */
    private final TextureFactory textureFactory;
    
    /** Used for performing OpenGL activities the proper context. */
    private final GLSurfaceView view;
    
    private Menu menu;
    
    private final float backgroundPanelHeight = 0.2f;
    private final int backgroundPanelVBO = DrawUtils.buildUnitSquarePcVbo(Color.HUD_BACKGROUND_PANEL_BLUE);
    
    private final float abilityPowerHeight = 0.20f * backgroundPanelHeight;
    private int         abilityPowerTexture = -1;
    private float       abilityPowerWidth;
    
    private final float abilityPowerIconHeight = 0.25f * backgroundPanelHeight;
    private final int   abilityPowerIconTexture;
    private final float abilityPowerIconWidth;
    
    private final float heartIconHeight = 0.25f * backgroundPanelHeight;
    private final int   heartIconTexture;
    private final float heartIconWidth;
    
    private final float menuGemHeight = 0.3f;
    private final int   menuGemTexture;
    private final float menuGemWidth;
    
    private StatsBar    abilityBar;
    private StatsBar    healthBar;
    private final float statsBarHeight;
    private final float statsBarWidth = 0.35f;
    
    // Unit specific items
    private volatile Unit   activeUnit = null;
    private float           currentAbilityPower;
    private float           currentHealth;
    private float           currentMaxAbilityPower;
    private float           currentMaxHealth;
    
    private final float healthHeight = 0.20f * backgroundPanelHeight;
    private int         healthTexture = -1;
    private float       healthWidth;
    
    private final float profileHeight = 0.5f;
    private final float profileWidth;
    
    private final float titleHeight = 0.25f * backgroundPanelHeight; 
    private int         titleTexture = -1;
    private float       titleWidth;
    
    /**
     * Constructor.
     */
    @Inject
    public UnitHUD(@Named("AbilityPowerTexture") final int abilityPowerIconTexture,
                   final ActionMenu actionMenu,
                   final SimpleColorShader colorShader,
                   final Device device,
                   @Named("HeartTexture") final int heartIconTexture,
                   final Injector injector,
                   @Named("MenuGearTexture") final int menuGemTexture,
                   final MenuFactory menuFactory,
                   final StatsBarFactory statsBarFactory,
                   final TextureFactory textureFactory,
                   final SimpleTexturedShader texturedShader,
                   final GLSurfaceView view) { 
        this.abilityPowerIconTexture = abilityPowerIconTexture;
        this.abilityPowerIconWidth = abilityPowerIconHeight / device.getAspectRatio();
        this.actionMenu = actionMenu;
        this.colorShader = colorShader;
        this.device = device;
        this.gemWidth = this.gemHeight / device.getAspectRatio();
        this.heartIconTexture = heartIconTexture;
        this.heartIconWidth = heartIconHeight / device.getAspectRatio();
        this.injector = injector;
        this.menu = menuFactory.create(MenuFactory.Type.PROTOTYPE);
        this.menuGemTexture = menuGemTexture;
        this.menuGemWidth = menuGemHeight / device.getAspectRatio();
        this.profileWidth = profileHeight / device.getAspectRatio();
        this.textureFactory = textureFactory;
        this.texturedShader = texturedShader;
        this.view = view;
        
        statsBarHeight = 0.2f * backgroundPanelHeight;
        abilityBar = statsBarFactory.create(
                statsBarHeight, statsBarWidth, Color.ABILITY_BAR_YELLOW, Color.BLACK);
        healthBar = statsBarFactory.create(
                statsBarHeight, statsBarWidth, Color.HEALTH_BAR_RED, Color.BLACK);
    }
    
    public synchronized Unit getActiveUnit() { return activeUnit; }
    
    @Override
    public boolean handleClick(NormalizedPoint2D clickLocation) {
        // Check if the click occurred on the action menu
        if (actionMenu.isActive() && actionMenu.handleClick(clickLocation)) {
            return true;
        }
        
        // Check if the click occurred on the menu gear
        final float xPos = 0.5f - menuGemWidth / 2.0f;
        final float yPos = -0.5f + menuGemHeight / 2.0f;
        if (InputHelper.isTouched(new Point2D(xPos, yPos), menuGemWidth, menuGemHeight, clickLocation)) {
            Log.i("ClickHandler", "Menu gear has been clicked");
          
            ((ExpandingMenu) menu).animate();
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean handleDrag(NormalizedPoint2D moveVector) { return false; }
    
    @Override
    public boolean handleDrop(NormalizedPoint2D dropLocation) { return false; }
    
    /** {@inheritDoc} */
    @Override
    public boolean handleLongPress(NormalizedPoint2D pressLocation) {
        try {
            final BattleRenderer renderer = injector.getInstance(BattleRenderer.class);
            final GridPoint2D pressedArea = renderer.convert(pressLocation);
    
            // If the pressed tile is not part of the battlefield 
            final Battlefield battlefield = injector.getInstance(Battlefield.class);
            final Optional<Tile> pressedTile = battlefield.getTile(pressedArea);
            if (!pressedTile.isPresent()) {
                Log.e("INPUT", "Pressed tile is not part of the battlefield!");
                return false;
            }
            
            // If there is no unit in the pressed tile
            final Unit pressedUnit = pressedTile.get().getOccupant();
            if (pressedUnit == null) {
                Log.e("INPUT", "Pressed tile does not have an occupant!");
                return false;
            }
            
            Log.i("INPUT", "Selecting long pressed unit!");
            battlefield.pickTile(pressedArea);
            
            Log.i("INPUT", "Updating the action menu...");
            actionMenu.setActingUnit(pressedUnit);
            actionMenu.setIsActive(true);
            return true;
        } catch (UnprojectionException ex) {
            Log.w("INPUT", "Failed to unproject the click location!");
            return false;
        }
    }

    @Override
    public boolean handlePickUp(NormalizedPoint2D touchLocation) { return false; }

    @Override
    public boolean handleZoom(float zoomFactor) { return false; }
    
    /** {@inheritDoc} */
    public synchronized final void render() {
        // Disable depth testing for the flat "on-screen" HUD
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        MVP mvp = new MVP();
        mvp.push(MVP.Type.PROJECTION, getProjection());
        
        if (activeUnit != null) {
            // Set up the background panel
            float[] model = mvp.peekCopy(MVP.Type.MODEL);
            Matrix.setIdentityM(model, Constants.BEGINNING_OF_BUFFER);
            Matrix.translateM(model, Constants.BEGINNING_OF_BUFFER, 0.0f, 
                    -0.5f + backgroundPanelHeight / 2.0f, 0.0f);
            mvp.push(MVP.Type.MODEL, model);
            Matrix.scaleM(model, Constants.BEGINNING_OF_BUFFER, 1.0f, backgroundPanelHeight, 1.0f);
            
            // Draw the panel
            colorShader.activate();
            colorShader.setMVPMatrix(mvp.collapseM(model));
            colorShader.setVBO(backgroundPanelVBO);
            colorShader.draw();

            renderStatsBar(mvp);
            renderUnitProfile(mvp);
            renderUnitName(mvp);
            renderIcons(mvp);
            renderUnitStats(mvp);
            
            mvp.pop(MVP.Type.MODEL);
        }
        
        if (actionMenu.isActive()) {
            actionMenu.render(mvp);
        }
        renderMenu(mvp);
        //renderMenuGem(mvp);
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
    
    private void renderStatsBar(final MVP mvp) {
        colorShader.activate();
        
        // Update the stats bars with the current stats
        abilityBar.setCurrentStat(activeUnit.getChargePoints());
        abilityBar.setMaxStat(activeUnit.getMaxChargePoints());
        healthBar.setCurrentStat(activeUnit.getHealth());
        healthBar.setMaxStat(activeUnit.getMaxHealth());
        
        // Render the health bar
        healthBar.render(mvp);
        
        // Render the ability bar
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.BEGINNING_OF_BUFFER, 0.0f, -0.3f * backgroundPanelHeight, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        abilityBar.render(mvp);
        mvp.pop(MVP.Type.MODEL);
    }
    
    /**
     * Render the ability/health icons.
     */
    private void renderIcons(final MVP mvp) {
        texturedShader.activate();
        
        // Draw the heart symbol
        float[] modelView = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(modelView, Constants.BEGINNING_OF_BUFFER, -statsBarWidth / 2.0f - heartIconWidth, 
                0.0f, 0.0f);
        Matrix.scaleM(modelView, Constants.BEGINNING_OF_BUFFER, heartIconWidth, heartIconHeight, 1.0f);
        texturedShader.setMVPMatrix(mvp.collapseM(modelView));
        texturedShader.setTexture(heartIconTexture);
        texturedShader.draw();
        
        // Draw the ability power symbol
        modelView = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(modelView, Constants.BEGINNING_OF_BUFFER, -statsBarWidth / 2.0f - abilityPowerIconWidth, 
                -0.3f * backgroundPanelHeight, 0.0f);
        Matrix.scaleM(modelView, Constants.BEGINNING_OF_BUFFER, abilityPowerIconWidth, abilityPowerIconHeight, 1.0f);
        texturedShader.setMVPMatrix(mvp.collapseM(modelView));
        texturedShader.setTexture(abilityPowerIconTexture);
        texturedShader.draw();
    }
    
    /** Renders the menu if its selected. */
    private void renderMenu(final MVP mvp) {
        menu.render(mvp);
    }
    
    /**
     * Renders the "Menu" gear.
     */
    private void renderMenuGem(final MVP mvp) {
        texturedShader.activate();
        
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, 0.5f - gemWidth / 2.0f, -0.5f + gemHeight / 2.0f, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, gemWidth, gemHeight, 1.0f);

        texturedShader.setMVPMatrix(mvp.collapseM(model));
        texturedShader.setTexture(menuGemTexture);
        texturedShader.draw();
    }
    
    /**
     * Render's the selected unit's name.
     */
    private void renderUnitName(final MVP mvp) {
        texturedShader.activate();
        
        final float[] modelView = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(modelView, Constants.BEGINNING_OF_BUFFER, 
                (-statsBarWidth + titleWidth) / 2.0f  - 1.5f * heartIconWidth, 
                0.3f * backgroundPanelHeight, 0.0f);
        Matrix.scaleM(modelView, Constants.BEGINNING_OF_BUFFER, titleWidth, titleHeight, 0.0f);
        
        texturedShader.setMVPMatrix(mvp.collapseM(modelView));
        texturedShader.setTexture(titleTexture);
        texturedShader.draw();
    }
   
    /**
     * Renders the active unit's health and ability stats.
     */
    private void renderUnitStats(final MVP mvp) {
        texturedShader.activate();
        
        // Create the health stats texture
        if (((int) activeUnit.getChargePoints() != (int) currentAbilityPower)
                || ((int) activeUnit.getMaxChargePoints() != (int) currentMaxAbilityPower)) {
            if (abilityPowerTexture != -1) {
                GLES20.glDeleteTextures(1, new int[]{abilityPowerTexture}, 
                        Constants.BEGINNING_OF_BUFFER);
            }
            
            String text = Integer.toString((int) activeUnit.getChargePoints()) + "/"
                    + Integer.toString((int) activeUnit.getMaxChargePoints());
            final float[] aspectRatio = new float[1];
            abilityPowerTexture = textureFactory.texturizeText(text, Color.WHITE, Paint.Align.LEFT, 60.0f, aspectRatio);
            abilityPowerWidth = abilityPowerHeight * aspectRatio[0] * backgroundPanelHeight * device.getAspectRatio();
            currentAbilityPower = activeUnit.getChargePoints();
            currentMaxAbilityPower = activeUnit.getMaxChargePoints();
        }
        
        // Create the ability power texture
        if (((int) activeUnit.getHealth() != (int) currentHealth)
                || ((int) activeUnit.getMaxHealth() != (int) currentMaxHealth)) {
            if (healthTexture != -1) {
                GLES20.glDeleteTextures(1, new int[]{healthTexture}, 
                        Constants.BEGINNING_OF_BUFFER);
            }
            
            String text = Integer.toString((int) activeUnit.getHealth()) + "/"
                    + Integer.toString((int) activeUnit.getMaxHealth());
            final float[] aspectRatio = new float[1];
            healthTexture = textureFactory.texturizeText(text, Color.WHITE, Paint.Align.LEFT, 60.0f, aspectRatio);
            healthWidth = healthHeight * aspectRatio[0] * backgroundPanelHeight * device.getAspectRatio();
            currentHealth = activeUnit.getHealth();
            currentMaxHealth = activeUnit.getMaxHealth();
        }
        
        // Render the health stats
        float[] modelView = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(modelView, Constants.BEGINNING_OF_BUFFER,
                (statsBarWidth + healthWidth) / 2.0f + healthBar.getBackgroundEdgeWidth(), 0.0f, 0.0f);
        Matrix.scaleM(modelView, Constants.BEGINNING_OF_BUFFER, healthWidth, healthHeight, 0.0f);
        texturedShader.setMVPMatrix(mvp.collapseM(modelView));
        texturedShader.setTexture(healthTexture);
        texturedShader.draw();
        
        // Render the ability power stats
        modelView = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(modelView, Constants.BEGINNING_OF_BUFFER, 
                (statsBarWidth + abilityPowerWidth) / 2.0f + abilityBar.getBackgroundEdgeWidth(), 
                -0.3f * backgroundPanelHeight, 0.0f);
        Matrix.scaleM(modelView, Constants.BEGINNING_OF_BUFFER, abilityPowerWidth, abilityPowerHeight, 0.0f);
        texturedShader.setMVPMatrix(mvp.collapseM(modelView));
        texturedShader.setTexture(abilityPowerTexture);
        texturedShader.draw();
    }
    
    /**
     * Render's the selected unit's profile image.
     */
    private void renderUnitProfile(final MVP mvp) {
        texturedShader.activate();
        
        final float[] modelView = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(modelView, Constants.BEGINNING_OF_BUFFER, -0.5f + profileWidth / 2.0f,
                (profileHeight - backgroundPanelHeight) / 2.0f, 0.0f);
        Matrix.scaleM(modelView, Constants.BEGINNING_OF_BUFFER, profileWidth, profileHeight, 0.0f);
        
        texturedShader.setMVPMatrix(mvp.collapseM(modelView));
        texturedShader.setTexture(activeUnit.getProfileTexture());
        texturedShader.draw();
    }
   
    public synchronized void setActingUnit(final Unit unit) {
        actionMenu.setActingUnit(unit);
        actionMenu.setIsActive(true);
    }
    
    public synchronized void setActiveUnit(final Unit unit) {  
        if (unit != null) {
            // Submit the texture generation job to the OpenGL thread for execution
            view.queueEvent(new Runnable() { 
                public void run() {
                    if (titleTexture != 1) {
                        GLES20.glDeleteTextures(1, new int[]{titleTexture}, Constants.NO_OFFSET);
                    }
                    
                    final float[] titleAspectRatio = new float[1];
                    titleTexture = textureFactory.texturizeText(unit.getName(), Color.WHITE, 
                            Paint.Align.LEFT, 60.0f, titleAspectRatio);
                    titleWidth = titleHeight * titleAspectRatio[0] * backgroundPanelHeight * device.getAspectRatio();
                }});
    
            currentAbilityPower = -1.0f;
            currentHealth = -1.0f;
            currentMaxAbilityPower = -1.0f;
            currentMaxHealth = -1.0f;
        }
        
        activeUnit = unit;
    }
}
