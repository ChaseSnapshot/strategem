package com.sneaky.stratagem.graphics.widgets.actionmenu;

import android.opengl.Matrix;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.Executable;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.glyphs.GlyphString;
import scatcat.graphics.glyphs.GlyphString.GlyphStringFactory;
import scatcat.graphics.shaders.SimpleTexturedShader;

public class ActionMenuItem implements RenderableMVP {
    private final Executable action;
    
    private final float aspectRatio;
    
    private final int backgroundTexture;
    
    private final Injector injector;
    
    private final GlyphString text;
    
    private final int unselectableTexture;
    
    @Inject
    protected ActionMenuItem(@Assisted("aspectRatio") final float aspectRatio,
                             @Named("ActionMenuItemBackground") final int backgroundTexture,
                             @Assisted("clickAction") final Executable action,
                             final GlyphStringFactory glyphStringFactory,
                             final Injector injector,
                             @Named("ActionMenuItemUnselectable") final int unselectableTexture) {
        this.aspectRatio = aspectRatio;
        this.backgroundTexture = backgroundTexture;
        this.action = action;
        this.injector = injector;
        this.unselectableTexture = unselectableTexture;
        
        final float height = 0.5f;
        this.text = glyphStringFactory.create(action.getName(), height);
        this.text.setRelativeWidth(0.9f);
    }
    
    public interface ActionMenuItemFactory {
        ActionMenuItem create(@Assisted("aspectRatio") final float aspectRatio,
                              @Assisted("clickAction") final Executable clickAction);
    }
    
    public boolean handleClick() {
        if (action.isExecutable()) {
            action.execute();
            
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void render(MVP mvp) {
        final float contentSize = 0.9f;
        
        SimpleTexturedShader shader = injector.getInstance(SimpleTexturedShader.class);
        shader.activate();
        
        // Draw the background texture
        shader.setMVPMatrix(mvp.collapse());
        shader.setTexture(backgroundTexture);
        shader.draw();
        
        // If the click action has an icon
        if (action.hasIconTexture()) {
            drawIcon(mvp, shader);
        }
        
        drawActionName(mvp, contentSize);
        
        // If the action cannot be performed
        if (!action.isExecutable()) {
            // Draw the unavailable overlay
            shader.setMVPMatrix(mvp.collapse());
            shader.setTexture(unselectableTexture);
            shader.draw();
        }
    }
    
    private void drawActionName(final MVP mvp, final float contentSize) {
        final float[] nameModel = mvp.peekCopy(MVP.Type.MODEL);
        
        // Move to the left edge of the menu item
        Matrix.translateM(nameModel, Constants.NO_OFFSET, -0.45f, 0.0f, 0.0f);
        
        // Shrink the content text to the correct size
        float nameWidth = (1.0f / aspectRatio);
        Matrix.scaleM(nameModel, Constants.NO_OFFSET, nameWidth, 1.0f, 1.0f);
        
        // Align the left edge of the text with the left edge of the menu item
        Matrix.translateM(nameModel, Constants.NO_OFFSET, text.getWidth() / 2.0f, 0.0f, 0.0f);
        
        // Perform the text draw
        mvp.push(MVP.Type.MODEL, nameModel);
        text.render(mvp);
        mvp.pop(MVP.Type.MODEL);
    }
      
    private void drawIcon(final MVP mvp, final SimpleTexturedShader shader) {
        final Device device = injector.getInstance(Device.class);
        final float[] iconModel = mvp.peekCopy(MVP.Type.MODEL);
        
        // Move to the right edge of the entire menu item
        Matrix.translateM(iconModel, Constants.NO_OFFSET, 0.5f, 0.0f, 0.0f);
        
        // Scale the icon to be 1:1 square to the menu item height
        Matrix.scaleM(iconModel, Constants.NO_OFFSET, 1.0f / aspectRatio / device.getAspectRatio(), 1.0f, 1.0f);
        
        // Align the right edge of the icon to the right edge of the menu item
        Matrix.translateM(iconModel, Constants.NO_OFFSET, -0.5f, 0.0f, 0.0f);
        
        // Shrink the icon size down to fit in the margins
        final float contentSize = 0.5f;
        Matrix.scaleM(iconModel, Constants.NO_OFFSET, contentSize, contentSize, 1.0f);
        
        // Perform the icon draw
        shader.setMVPMatrix(mvp.collapseM(iconModel));
        shader.setTexture(action.getIconTexture());
        shader.draw();
    }
}
