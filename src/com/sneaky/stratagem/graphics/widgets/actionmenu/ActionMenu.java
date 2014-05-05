package com.sneaky.stratagem.graphics.widgets.actionmenu;

import java.util.ArrayList;
import java.util.List;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.points.NormalizedPoint2D;
import scatcat.general.points.Point2D;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.input.ClickHandler;
import scatcat.input.InputHelper;

import android.opengl.Matrix;

import static com.google.common.base.Preconditions.checkState;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.sneaky.stratagem.actions.Executable;
import com.sneaky.stratagem.graphics.widgets.actionmenu.ActionMenuItem.ActionMenuItemFactory;
import com.sneaky.stratagem.units.Unit;

public class ActionMenu implements ClickHandler, RenderableMVP {
    private float height;
    
    private final Injector injector;
    
    private boolean isActive = false;
    
    private final float menuItemHeight = 0.1f;
    
    private final List<ActionMenuItem> menuItems = new ArrayList<ActionMenuItem>();
    
    private float width;
    
    @Inject
    protected ActionMenu(final Injector injector) {
        this.injector = injector;
    }
    
    @Override
    public boolean handleClick(NormalizedPoint2D clickLocation) {
        Point2D position = new Point2D(0.0f, (height - menuItemHeight) / 2.0f);
        
        isActive = false;
        
        for (ActionMenuItem menuItem : menuItems) {
            if (InputHelper.isTouched(position, width, menuItemHeight, clickLocation)) {
                return menuItem.handleClick();
            }
            
            position.addToY(-menuItemHeight);
        }
        
        return false;
    }
    
    public boolean isActive() { return isActive; }
    
    @Override
    public void render(MVP mvp) {
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        
        // Move up half the total height of the menu to accommodate iterative item drawing
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, (height - menuItemHeight) / 2.0f, 0.0f);
        
        // Draw the menu items
        for (ActionMenuItem menuItem : menuItems) {
            mvp.push(MVP.Type.MODEL, model);
            
            // Size the menu item
            Matrix.scaleM(model, Constants.NO_OFFSET, width, menuItemHeight, 1.0f);
            
            // Draw the menu item
            mvp.push(MVP.Type.MODEL, model);
            menuItem.render(mvp);
            mvp.pop(MVP.Type.MODEL);
            
            // Move down to the next menu item location
            model = mvp.pop(MVP.Type.MODEL);
            Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, -menuItemHeight, 0.0f);
        }
    }
    
    public void setActingUnit(final Unit actingUnit) {
        List<Executable> actions = actingUnit.getActions();
        
        // Remove any previously existing menu items
        menuItems.clear();
        
        // Determine the total height of the action menu
        height = menuItemHeight * actions.size();
        
        // Determine the longest action name
        float longestActionName = 0.0f;
        for (Executable action : actions) {
            final String actionName = action.getName();
            checkState(actionName != null, "Action name has not been set!");
            
            if (actionName.length() > longestActionName) {
                longestActionName = actionName.length();
            }
        }
        
        // Determine the final width of the menu
        final Device device = injector.getInstance(Device.class);
        width = (0.1f + 0.15f * longestActionName / 4.0f) / device.getAspectRatio(); 
        
        // Build each of the menu items
        for (Executable action : actions) {
            ActionMenuItemFactory menuItemFactory = injector.getInstance(ActionMenuItemFactory.class);
            menuItems.add(menuItemFactory.create(width / menuItemHeight, action));
        }
    }
    
    public void setIsActive(final boolean isActive) { this.isActive = isActive; }
}
