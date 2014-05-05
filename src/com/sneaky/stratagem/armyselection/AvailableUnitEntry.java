package com.sneaky.stratagem.armyselection;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.graphics.MVP;
import scatcat.graphics.glyphs.GlyphString;
import scatcat.graphics.glyphs.GlyphString.GlyphStringFactory;
import scatcat.graphics.shaders.SimpleTexturedShader;
import android.opengl.Matrix;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.sneaky.stratagem.units.Unit;

/** Unit entry for units available to be enlisted in the player's army. */
public class AvailableUnitEntry extends UnitEntry {
    // "COST" label properties
    private final GlyphString costLabel;
    private final float costLabelXOffset = 0.02f * getWidth();
    private final float costLabelYOffset = 0.30f * getHeight();
    
    // Unit cost label properties 
    private final GlyphString cost;
    private final float costHeight = 0.6f * getHeight();
    private final float costYOffset = -0.1f * getHeight();
    
    private final SimpleTexturedShader shader;
    
    @Inject
    protected AvailableUnitEntry(@Named("UnitEntryBackgroundTexture") final int backgroundTexture,
                                 final Device device,
                                 final GlyphStringFactory glyphStringFactory,
                                 @Named("EntryOrbTexture") final int orbTexture,
                                 final SimpleTexturedShader shader,
                                 @Assisted("unit") final Unit unit,
                                 @Assisted("height") final float height,
                                 @Assisted("width") final float width) {
        super(backgroundTexture, device, glyphStringFactory, height, orbTexture, shader, unit, width);
        
        this.cost = glyphStringFactory.create(Integer.toString(unit.getEnlistCost()), costHeight);
        this.costLabel = glyphStringFactory.create("COST", getHeight() * 0.35f);
        this.shader = shader;
    }
    public interface AvailableUnitEntryFactory {
        AvailableUnitEntry create(@Assisted("unit") final Unit unit,
                                  @Assisted("height") final float height,
                                  @Assisted("width") final float width);
    }
    
    @Override
    public void render(final MVP mvp) {
        final float backgroundRightEdge = 0.5f * getWidth();
        
        super.render(mvp);
        shader.activate();
        
        // Render the the "COST" label above the point cost of the unit
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        float xPosition = backgroundRightEdge - costLabel.getWidth() / 2.0f - costLabelXOffset;
        Matrix.translateM(model, Constants.NO_OFFSET, xPosition, costLabelYOffset, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        costLabel.render(mvp);
        mvp.pop(MVP.Type.MODEL);
        
        // Render the point cost of the unit
        model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, xPosition, costYOffset, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        cost.render(mvp);
        mvp.pop(MVP.Type.MODEL);
    }
}
