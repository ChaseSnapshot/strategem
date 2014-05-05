package com.sneaky.stratagem.huds;

import scatcat.graphics.DrawUtils;
import scatcat.input.ClickHandler;
import scatcat.input.Draggable;
import scatcat.input.LongPressHandler;
import scatcat.input.ZoomHandler;

/**
 * Base class for all Heads Up Displays.
 * 
 * @author R. Matt McCann
 */
public abstract class HUD implements ClickHandler, LongPressHandler, Draggable, ZoomHandler {
    /**
     * Constructor.
     */
    public HUD() {
    }
    
    public abstract void render();
  
    public float[] getProjection() {
        return mProjection;
    }
    
    private final float[] mProjection = DrawUtils.gen2DOrthoProjection();
}
