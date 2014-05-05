package com.sneaky.stratagem.graphics.widgets.menus;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.points.CartesianScreenPoint2D;
import scatcat.general.points.NormalizedPoint2D;
import scatcat.graphics.MVP;
import scatcat.graphics.glyphs.GlyphString;
import scatcat.graphics.glyphs.GlyphString.GlyphStringFactory;
import scatcat.graphics.shaders.SimpleTexturedShader;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.Executable;
import com.sneaky.stratagem.units.Unit;

public class ActionMenu extends Menu {
    /** Whether or not the action menu is being displayed. */
    private boolean isActive = false;
    
    final float menuItemHeight = 0.1f;
    float menuItemWidth;
    
    /** Textures */
    private final int bottomTexture;
    private final int topTexture;
    private final int unselectableTexture;
    
    private final SimpleTexturedShader shader;
    
    
    private final int backgroundTexture;
    private final GlyphStringFactory glyphStringFactory;
    private final Injector injector;
    
    @Inject
    protected ActionMenu(@Named("ActionMenuBottom") final int bottomTexture,
                         @Named("ActionMenuBackground") final int backgroundTexture,
                         @Named("ActionMenuTop") final int topTexture,
                         @Named("ActionMenuUnselectable") final int unselectableTexture,
                         final Device device,
                         final GlyphStringFactory glyphStringFactory,
                         final Injector injector,
                         final SimpleTexturedShader shader,
                         final GLSurfaceView surfaceView) {
        this.backgroundTexture = backgroundTexture;
        this.glyphStringFactory = glyphStringFactory;
        this.bottomTexture = bottomTexture;
        this.injector = injector;
        this.menuItemWidth = 0.4f / device.getAspectRatio();
        this.shader = shader;
        this.topTexture = topTexture;
        this.unselectableTexture = unselectableTexture;
    }
    
    @Override
    public boolean handleClick(final NormalizedPoint2D clickLocation) {
        final boolean handledClick = super.handleClick(clickLocation);

        isActive = !handledClick;
        
        return handledClick;
    }
    
    public final boolean isActive() { return isActive; }
  
    @Override
    public void render(final MVP mvp) {
        shader.activate();
        
        // Render the menu items
        super.render(mvp);
        
        // Render the top of the menu
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        final float headerHeight = menuItemWidth / 8.0f;
        Matrix.translateM(model, Constants.NO_OFFSET, getPosition().getX(), 
                getPosition().getY() + menuItemHeight / 2.0f, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, menuItemWidth, headerHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(topTexture);
        shader.draw();
        
        // Render the bottom of the menu
        model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, getPosition().getX(), 
                getPosition().getY() - getHeight() + menuItemHeight / 2.0f, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, menuItemWidth, headerHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(bottomTexture);
        shader.draw();
    }
    
    public final void setActingUnit(final Unit actingUnit) { 
        // Remove any previous menu items
        clearMenuItems();
        
        // Populate the action menu with the selected unit's actions
        menuItemWidth = 0.0f;
        for (Executable action : actingUnit.getActions()) {
            MenuItem menuItem = injector.getInstance(MenuItem.class);
            menuItem.setBackgroundTexture(backgroundTexture);
            menuItem.setUnselectableOverlay(unselectableTexture);
            menuItem.setClickAction(action);
        
            final GlyphString text = glyphStringFactory.create(action.getName(), 0.9f);
            text.setRelativeWidth(0.5f);
            if (text.getWidth() * menuItemHeight > menuItemWidth) { 
                menuItemWidth = text.getWidth() * menuItemHeight;
            }
            
            menuItem.setDisplayedText(text);
            menuItem.setHeight(menuItemHeight);
            
            addMenuItem(menuItem);
        }
        
        // Resize the width of the menu to fit all of the action names
        for (MenuItem menuItem : getMenuItems()) {
            menuItem.setWidth(menuItemWidth);
        }
        
        // Center the menu in the middle of the screen
        CartesianScreenPoint2D menuCenterPoint = new CartesianScreenPoint2D();
        menuCenterPoint.setX(0.0f); // Centered horizontally
        menuCenterPoint.setY(getHeight() / 2.0f); // Centered vertically
        setDrawDirection(Menu.DrawDirection.DOWN);
        setCenterPoint(menuCenterPoint);
    }
    public final void setIsActive(final boolean isActive) {
        this.isActive = isActive;
    }
}
