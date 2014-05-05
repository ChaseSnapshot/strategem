package com.sneaky.stratagem.graphics.widgets.menus;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.sneaky.stratagem.actions.Executable;

import android.opengl.Matrix;
import android.util.Log;
import scatcat.general.Cleanable;
import scatcat.general.Constants;
import scatcat.general.Dimension;
import scatcat.general.points.CartesianScreenPoint2D;
import scatcat.general.points.NormalizedPoint2D;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.glyphs.GlyphString;
import scatcat.graphics.glyphs.GlyphString.GlyphStringFactory;
import scatcat.graphics.shaders.SimpleTexturedShader;
import scatcat.input.ClickHandler;

/**
 * Item widget in the menu widget.
 * 
 * @author R. Matt McCann
 */
public class MenuItem implements RenderableMVP, ClickHandler, Cleanable {
    /** Background texture. */
    private int backgroundTexture;
    
    /** Screen location at which the rendering of this menu item occurs. Used for touch handling calculations. */
    private CartesianScreenPoint2D centerPoint;
    
    /** The action executed when the menu item is clicked. */
    private Optional<Executable> clickAction = Optional.<Executable>absent();
    
    /** Text displayed in the menu item. */
    private String displayedText;
    
    /** Dimensions of the displayed text. */
    private Dimension displayedTextSize = new Dimension();
    
    /** Texture for displayed the displayed text. */
    private int displayedTextTexture = -1;
    
    /** Direction in which to expand for the sub-menu. */
    private ExpandDirection expandDirection;
    
    private final GlyphStringFactory glyphStringFactory;
    
    /** Displaying height of the menu item. */
    private float height;
    
    /** Whether or not the menu item is currently selected. */
    private boolean isSelected = false;
    
    /** Sub-menu displayed when the menu item is selected. */
    private Optional<Menu> menu = Optional.<Menu>absent();
    
    /** Background texture displayed when the menu item is selected. */
    private int selectedBackgroundTexture;
    
    private final SimpleTexturedShader shader;
    
    private GlyphString text;
    
    /** Overlay used when the menu item is unselectable. */
    private int unselectableOverlay;
    
    /** Displaying width of the menu item. */
    private float width;
    
    public enum ExpandDirection {
        LEFT,
        RIGHT
    }
    
    @Inject
    protected MenuItem(final GlyphStringFactory glyphStringFactory,
                       final SimpleTexturedShader shader) {
        this.glyphStringFactory = glyphStringFactory;
        this.shader = shader;
    }
    
    /**
     * Runs the click action's clean up routine.
     */
    @Override
    public final void cleanUp() {
        isSelected = false;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean handleClick(final NormalizedPoint2D clickLocation) {
        if (!clickAction.isPresent()) {
            return false;
        }
        
        // Determine if the click occurred within the menu item
        if ((clickLocation.getX() >= centerPoint.getX() - width / 2.0f) &&
            (clickLocation.getX() <= centerPoint.getX() + width / 2.0f) &&
            (clickLocation.getY() >= centerPoint.getY() - height / 2.0f) &&
            (clickLocation.getY() <= centerPoint.getY() + height / 2.0f)) {
            Log.i("ClickHandler", "Menu item '" + displayedText + "' handled click!");
            
            // If the menu item is already selected, deselect it
            if (isSelected) {
                // Clean up the click action if there is one
                if (clickAction.isPresent()) {
                    clickAction.get().cleanUp();
                }
                
                isSelected = false;
            }
            // The menu item isn't selected so execute the click action
            else {
                // Execute the click action
                if (clickAction.isPresent()) {
                    clickAction.get().execute();
                }

                isSelected = true;
            }

            return true;
        }
        // Check if the sub-menu was clicked if a sub-menu exists
        else if (menu.isPresent()) {
            return menu.get().handleClick(clickLocation);
        }
        
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public void render(MVP mvp) {
        shader.activate();
        
        // Draw the background panel
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.scaleM(model, Constants.NO_OFFSET, width, height, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        if (isSelected) shader.setTexture(selectedBackgroundTexture);
        else shader.setTexture(backgroundTexture);
        shader.draw();
        
        // Draw the foreground text
        Matrix.scaleM(model, Constants.NO_OFFSET, 1.0f / text.getWidth() * 0.9f, 0.9f, 1.0f);
        mvp.push(MVP.Type.MODEL, model);
        text.render(mvp);
        mvp.pop(MVP.Type.MODEL);
        
        // Draw the menu item as unselectable if necessary
        if (clickAction.isPresent() && !clickAction.get().isExecutable()) {
            shader.setTexture(unselectableOverlay);
            shader.draw();
        }
        
        // Draw the sub-menu if it exists and if the menu item is currently selected.
        if (isSelected && menu.isPresent()) {
            model = mvp.peekCopy(MVP.Type.MODEL);

            if (expandDirection == ExpandDirection.LEFT) {
                Matrix.translateM(model, Constants.NO_OFFSET, -width, 0.0f, 0.0f);
            } else {
                Matrix.translateM(model, Constants.NO_OFFSET, width, 0.0f, 0.0f);
            }
            
            mvp.push(MVP.Type.MODEL, model);
            menu.get().render(mvp);
            mvp.pop(MVP.Type.MODEL);
        }
    }

    public int getBackgroundTexture() {
        return backgroundTexture;
    }
    public String getDisplayedText() {
        return displayedText;
    }
    public Dimension getDisplayedTextSize() {
        return displayedTextSize;
    }
    public int getDisplayedTextTexture() {
        return displayedTextTexture;
    }
    public ExpandDirection getExpandDirection() {
        return expandDirection;
    }
    public float getHeight() {
        return height;
    }
    public boolean isSelected() {
        return isSelected;
    }
    public Menu getMenu() {
        return menu.get();
    }
    public int getSelectedBackgroundTexture() {
        return selectedBackgroundTexture;
    }
    public float getWidth() {
        return width;
    }

    public final void setClickAction(final Executable clickAction) {
        clickAction.setCaller(this);
        this.clickAction = Optional.<Executable>of(clickAction); 
    }
    
    public void setBackgroundTexture(int backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
    }
    public void setCenterPoint(final CartesianScreenPoint2D centerPoint) { this.centerPoint = centerPoint; }
    public void setDisplayedText(String displayedText) {
        // If there is already a text defined, delete the texture
        /**if (displayedTextTexture != -1) {
            
            final int numTextures = 1;
            GLES20.glDeleteTextures(numTextures, new int[]{displayedTextTexture}, 
                   Constants.NO_STRIDE);
        }
        
        // Generate the texture and size
        displayedTextTexture = TextureHelper.texturizeText(displayedText, 
                Color.BLACK, Paint.Align.CENTER, fontSize, displayedTextSize);
        this.displayedText = displayedText;**/
        text = glyphStringFactory.create(displayedText, 0.9f);
    }
    public void setDisplayedText(final GlyphString text) {
        this.text = text;
    }
    public void setExpandDirection(ExpandDirection expandDirection) {
        this.expandDirection = expandDirection;
    }
    public void setHeight(float height) {
        this.height = height;
    }
    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
    public void setMenu(Menu menu) {
        this.menu = Optional.of(menu);
    }
    public void setSelectedBackgroundTexture(int selectedBackgroundTexture) {
        this.selectedBackgroundTexture = selectedBackgroundTexture;
    }
    public final void setUnselectableOverlay(final int unselectableOverlay) { 
        this.unselectableOverlay = unselectableOverlay; }
    public void setWidth(float width) {
        this.width = width;
    }
}
