package com.sneaky.stratagem.graphics.widgets.menus;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Optional;

import android.opengl.Matrix;

import scatcat.general.Constants;
import scatcat.general.points.CartesianScreenPoint2D;
import scatcat.general.points.NormalizedPoint2D;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.input.ClickHandler;

/**
 * Menu widget.
 * 
 * @author R. Matt McCann
 */
public class Menu implements RenderableMVP, ClickHandler {
    /** Direction in which to draw the menu items. */
    private DrawDirection drawDirection;
    
    /** Items displayed in the menu. */
    private List<MenuItem> menuItems = new ArrayList<MenuItem>();

    /** Where the menu is positioned on the screen. */
    private CartesianScreenPoint2D position;
    
    /** The currently selected menu item. Used for easy clean-up. */
    private Optional<MenuItem> selectedMenuItem = Optional.<MenuItem>absent();
    
    public enum DrawDirection {
        DOWN,
        UP
    }
    
    /** {@inheritDoc} */
    @Override
    public void render(final MVP mvp) {
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        
        // Position the start of the menu
        Matrix.translateM(model, Constants.NO_OFFSET, position.getX(), position.getY(), 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        
        for (MenuItem menuItem : menuItems) {
            menuItem.render(mvp);
            
            model = mvp.peek(MVP.Type.MODEL);
            if (drawDirection == DrawDirection.DOWN) {
                Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, -menuItem.getHeight(), 0.0f);
            } else {
                Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, menuItem.getHeight(), 0.0f);
            }
        }
        
        mvp.pop(MVP.Type.MODEL);
    }
    
    public void addMenuItem(MenuItem menuItem) {
        menuItems.add(menuItem);
    }
    
    public void clearMenuItems() { menuItems.clear(); }
    
    @Override
    public boolean handleClick(NormalizedPoint2D clickLocation) {        
        for (MenuItem menuItem : menuItems) {
            if (menuItem.handleClick(clickLocation)) {
                
                // Clean up any actions caused by the previously clicked menu item if necessary
                if (selectedMenuItem.isPresent() && (selectedMenuItem.get() != menuItem)) {
                    selectedMenuItem.get().cleanUp();
                }
                
                // Save the clicked menu item for later clean up
                selectedMenuItem = Optional.<MenuItem>of(menuItem);
                
                return true;
            }
        }
        
        return false;
    }
    
    public final DrawDirection getDrawDirection() { return drawDirection; }
    public float getHeight() {
        float height = 0.0f;
        
        for (MenuItem menuItem : menuItems) {
            height += menuItem.getHeight();
        }
        
        return height;
    }
    protected final List<MenuItem> getMenuItems() { return menuItems; }
    protected final CartesianScreenPoint2D getPosition() { return position; }
    public float getWidth() {
        return menuItems.get(0).getWidth();
    }

    public void setCenterPoint(final CartesianScreenPoint2D centerPoint) {
        position = centerPoint;
        
        // Update the center points for each of the menu items
        float yMove = 0.0f;
        for (MenuItem menuItem : menuItems) {
            CartesianScreenPoint2D menuItemCenterPoint = new CartesianScreenPoint2D(centerPoint);
            menuItemCenterPoint.addToY(yMove);
            menuItem.setCenterPoint(menuItemCenterPoint);
            
            if (drawDirection == DrawDirection.UP) {
                yMove += menuItem.getHeight();
            } else {
                yMove -= menuItem.getHeight();
            }
        }
    }
    public void setDrawDirection(final DrawDirection drawDirection) {
        this.drawDirection = drawDirection;
    }
}
