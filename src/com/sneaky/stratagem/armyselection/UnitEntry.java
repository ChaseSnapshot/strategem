package com.sneaky.stratagem.armyselection;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.graphics.MVP;
import scatcat.graphics.glyphs.GlyphString;
import scatcat.graphics.glyphs.GlyphString.GlyphStringFactory;
import scatcat.graphics.shaders.SimpleTexturedShader;
import android.opengl.Matrix;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sneaky.stratagem.units.Unit;

/** Base unit entry in the Army Selection interface. */
public abstract class UnitEntry extends Entry {
    /** Background orb properties. */
    private final float orbHeight = 1.3f * getHeight();
    private final float orbLeftOffset = 0.02f * getWidth();
    private final int   orbTexture;
    private final float orbWidth;
    
    /** Unit profile size ratio relative to the background height. */
    private final float unitProfileHeight = 1.25f * getHeight();
    private final float unitProfileWidth;
    
    /** Unit name properties. */
    private final GlyphString unitName;
    private final float unitNameHeight = 0.5f * getHeight();
    private final float unitNameLeftOffset = 0.02f * getWidth();
    private final float unitNameRelativeLetterWidth = 1.0f;
    private final float unitNameLetterSpacing = 0.05f;
    
    /** Shader program for rendering. */
    private final SimpleTexturedShader shader;
    
    private final Unit unit;
    
    /** Injection constructor. */
    @Inject
    protected UnitEntry(@Named("UnitEntryBackgroundTexture") final int backgroundTexture,
                        final Device device,
                        final GlyphStringFactory glyphStringFactory,
                        final float height,
                        @Named("EntryOrbTexture") final int orbTexture,
                        final SimpleTexturedShader shader,
                        final Unit unit,
                        final float width) {
        super(backgroundTexture, height, shader, width);
        
        this.orbTexture = orbTexture;
        this.orbWidth = this.orbHeight / device.getAspectRatio();
        this.shader = shader;
        this.unit = unit;
        this.unitName = glyphStringFactory.create(unit.getName(), unitNameHeight);
        this.unitName.setRelativeWidth(unitNameRelativeLetterWidth);
        this.unitName.setLetterSpacing(unitNameLetterSpacing);
        this.unitProfileWidth = this.unitProfileHeight / device.getAspectRatio();
    }
    
    public final int getEnlistCost() { return unit.getEnlistCost(); }
    public final Unit getUnit() { return unit; } 
    
    @Override
    public void render(MVP mvp) {
        final float backgroundLeftEdge = -0.5f * getWidth();
        
        super.render(mvp);
        shader.activate();
        
        // Render the orb
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        float xPosition = backgroundLeftEdge + orbLeftOffset + orbWidth / 2.0f;
        Matrix.translateM(model, Constants.NO_OFFSET, xPosition, 0.0f, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, orbWidth, orbHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(orbTexture);
        shader.draw();
        
        // Render the unit profile
        model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, xPosition, 0.0f, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, unitProfileWidth, unitProfileHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(unit.getProfileTexture());
        shader.draw();
        
        // Render the name of the unit
        model = mvp.peekCopy(MVP.Type.MODEL);
        xPosition += orbWidth / 2.0f + unitNameLeftOffset + unitName.getWidth() / 2.0f;
        Matrix.translateM(model, Constants.NO_OFFSET, xPosition, 0.0f, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        unitName.render(mvp);
        mvp.pop(MVP.Type.MODEL);
    }
}
