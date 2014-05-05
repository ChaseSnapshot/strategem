package com.sneaky.stratagem.graphics.widgets.menus;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sneaky.stratagem.flow.GameFlowController;
import com.sneaky.stratagem.flow.Updatable;
import com.sneaky.stratagem.graphics.textures.TextureFactory;

import android.opengl.Matrix;
import com.sneaky.stratagem.R;
import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.points.CartesianScreenPoint2D;
import scatcat.graphics.MVP;
import scatcat.graphics.shaders.SimpleTexturedShader;
import scatcat.input.ClickHandlerManager;

public class ExpandingMenu extends Menu implements Updatable {
    /** Used to add/remove this as an active click handler. */
    //private ClickHandlerManager activeClickHandlers;
    
    private AnimationMode animationMode = AnimationMode.COLLAPSING;
    private final float animationVelocity = 2.25f; // Percentage of animation completed per second
    private final float expanded;
    private final float menuTopHeight;
    private final int menuTopTexture;
    private final GameFlowController updater;
    private float yPosition = 0.0f;
    
    private final SimpleTexturedShader shader;
    
    private enum AnimationMode {
        COLLAPSING,
        EXPANDING
    }
    
    /** Guice injection constructor. */
    @Inject
    protected ExpandingMenu(//final ClickHandlerManager activeClickHandlers,
                            final Device device,
                            @Named("MainMenuTop") final int menuTopTexture,
                            final GameFlowController updater,
                            final SimpleTexturedShader shader) {
        super();
        
        //this.activeClickHandlers = activeClickHandlers;
        this.expanded = device.getHeight() * 0.405f;
        this.menuTopHeight = device.getHeight() * 0.05f;
        this.menuTopTexture = menuTopTexture;
        this.shader = shader;
        this.updater = updater;
        
        setDrawDirection(DrawDirection.DOWN);
    }
    
    public void animate() {
        if (animationMode == AnimationMode.COLLAPSING) {
            animationMode = AnimationMode.EXPANDING;
        } else {
            animationMode = AnimationMode.COLLAPSING;
            //activeClickHandlers.remove(this);
        }
        
        updater.addUpdatable(this);
    }

    public float getOffset() {
        return yPosition;
    }
    
    @Override
    public void render(final MVP mvp) {
        shader.activate();
        
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, yPosition, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        
        // Render the top of the menu
        Matrix.translateM(model, Constants.NO_OFFSET, getPosition().getX(), getPosition().getY(), 0.0f);
        Matrix.translateM(model, Constants.NO_OFFSET, -0.1f, 0.0f, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, getWidth() * 1.1f, menuTopHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(menuTopTexture);
        shader.draw();
        
        // Render the rest of the menu
        model = mvp.pop(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, -menuTopHeight / 2.0f, 0.0f);
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, -getMenuItems().get(0).getHeight() / 2.0f, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        super.render(mvp);
        mvp.pop(MVP.Type.MODEL);
    }
    
    @Override
    public void setCenterPoint(final CartesianScreenPoint2D centerPoint) {
        // Adjust the starting center point to the center of the first menu item
        final int firstMenuItem = 0;
        float yOffset = centerPoint.getY();
        yOffset -= (menuTopHeight + getMenuItems().get(firstMenuItem).getHeight()) / 2.0f;
        centerPoint.setY(yOffset);
        
        // Update the center points for each of the menu items
        super.setCenterPoint(centerPoint);
    }

    @Override
    public void updateState(int updatesPerSecond) {
        if (animationMode == AnimationMode.COLLAPSING) {
            yPosition -= expanded * animationVelocity / updatesPerSecond;
            
            if (yPosition <= 0.0f) {
                updater.removeUpdatable(this);
            }
        } else if (animationMode == AnimationMode.EXPANDING) {
            yPosition += expanded * animationVelocity / updatesPerSecond;
            
            if (yPosition >= expanded) {
                //activeClickHandlers.add(this);
                updater.removeUpdatable(this);
            }
        } else {
            throw new RuntimeException("Illegal AnimationMode value!");
        }
    }
}
