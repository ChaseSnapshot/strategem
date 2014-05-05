package com.sneaky.stratagem.graphics.widgets.menus;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import com.sneaky.stratagem.R;
import com.sneaky.stratagem.graphics.textures.TextureFactory;
import com.sneaky.stratagem.huds.UnitHUD;

import scatcat.general.Device;
import scatcat.general.points.CartesianScreenPoint2D;

/**
 * Encapsulates the construction of menus.
 * 
 * @author R. Matt McCann
 */
public class MenuFactory {
    private final Device device;
    private final Injector injector;
    private final int menuItemBackground;
    private final int menuItemBackgroundSelected;
    private final int menuItemUnselectable;
    
    private final TextureFactory textureFactory;
    
    public enum Type {
        PROTOTYPE
    }
    
    @Inject
    protected MenuFactory(final Device device,
                          final Injector injector,
                          @Named("MenuItemBackground") final int menuItemBackground,
                          @Named("MenuItemBackgroundSelected") final int menuItemBackgroundSelected,
                          @Named("MenuItemUnselectable") final int menuItemUnselectable,
                          final TextureFactory textureFactory) {
        this.device = device;
        this.injector = injector;
        this.menuItemBackground = menuItemBackground;
        this.menuItemBackgroundSelected = menuItemBackgroundSelected;
        this.menuItemUnselectable = menuItemUnselectable;
        this.textureFactory = textureFactory;
    }
    
    public Menu create(Type type) {
        switch (type) {
            case PROTOTYPE:
                return createPrototype();
            default:
                throw new RuntimeException("Unknown menu type!");        
        }
    }
    
    private Menu createPrototype() {
        Menu menu = injector.getInstance(ExpandingMenu.class);
        float width = 0.3f;
        width *= 0.9f;
        final float aspectRatio = 3.0f;
        
        MenuItem item = injector.getInstance(MenuItem.class);
        //item.setBackgroundTexture(menuItemBackground);
        int menuItemTexture = textureFactory.loadTexture(R.drawable.menu_main_item); 
        int menuSpacerTexture = textureFactory.loadTexture(R.drawable.menu_main_spacer);
        item.setBackgroundTexture(menuSpacerTexture);
        item.setDisplayedText("");
        item.setExpandDirection(MenuItem.ExpandDirection.LEFT);
        item.setHeight(width / aspectRatio);
        item.setSelectedBackgroundTexture(menuItemBackgroundSelected);
        item.setUnselectableOverlay(menuItemUnselectable);
        item.setWidth(width);
        
        MenuItem item2 = injector.getInstance(MenuItem.class);
        //item2.setBackgroundTexture(menuItemBackground);
        item2.setBackgroundTexture(menuItemTexture);
        item2.setDisplayedText("ITEM");
        item2.setExpandDirection(MenuItem.ExpandDirection.LEFT);
        item2.setHeight(width / aspectRatio);
        item2.setSelectedBackgroundTexture(menuItemBackgroundSelected);
        item2.setUnselectableOverlay(menuItemUnselectable);
        item2.setWidth(width);
        
        MenuItem item3 = injector.getInstance(MenuItem.class);
        //item3.setBackgroundTexture(menuItemBackground);
        item3.setBackgroundTexture(menuItemTexture);
        //item3.setClickAction(new MoveAction());
        item3.setDisplayedText("MOVE");
        item3.setExpandDirection(MenuItem.ExpandDirection.LEFT);
        item3.setHeight(width / aspectRatio);
        item3.setSelectedBackgroundTexture(menuItemBackgroundSelected);
        item3.setUnselectableOverlay(menuItemUnselectable);
        item3.setWidth(width);
        
        MenuItem item4 = injector.getInstance(MenuItem.class);
        //item4.setBackgroundTexture(menuItemBackground);
        item4.setBackgroundTexture(menuItemTexture);
        //item4.setClickAction(new AttackAction());
        item4.setDisplayedText("ATTACK");
        item4.setExpandDirection(MenuItem.ExpandDirection.LEFT);
        item4.setHeight(width / aspectRatio);
        item4.setSelectedBackgroundTexture(menuItemBackgroundSelected);
        item4.setUnselectableOverlay(menuItemUnselectable);
        item4.setWidth(width);
        
        menu.addMenuItem(item4);
        menu.addMenuItem(item3);
        menu.addMenuItem(item2);
        menu.addMenuItem(item);
        menu.addMenuItem(item);
        
        final CartesianScreenPoint2D centerPoint = new CartesianScreenPoint2D();
        centerPoint.setX((device.getWidth() - width) / 2.0f);
        centerPoint.setY(-device.getHeight() / 2.0f + width);
        menu.setCenterPoint(centerPoint);
        
        return menu;
    }
}
