package com.sneaky.stratagem.armyselection;

import android.opengl.Matrix;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.sneaky.stratagem.units.Unit;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.points.NormalizedPoint2D;
import scatcat.general.points.Point2D;
import scatcat.graphics.MVP;
import scatcat.graphics.glyphs.GlyphString;
import scatcat.graphics.glyphs.GlyphString.GlyphStringFactory;
import scatcat.graphics.shaders.SimpleTexturedShader;
import scatcat.input.InputHelper;
import scatcat.input.UnpositionedClickHandler;

/** Unit entry for units enlisted in the player's army. */
public class EnlistedUnitEntry extends UnitEntry implements UnpositionedClickHandler {
    // Arrow properties
    private final int arrowTexture;
    private final float arrowSize = 0.40f * getHeight();
    private final float arrowXOffset = 0.01f * getWidth();
    private final float arrowYOffset = 0.05f * getHeight();
    
    // "ENLISTED" label properties
    private final GlyphString enlistedLabel;
    private final float enlistedLabelXOffset = 0.10f * getWidth();
    private final float enlistedLabelYOffset = 0.35f * getHeight();
    
    // Number of enlisted units label properties
    private int numEnlisted = 0;
    private final float numEnlistedHeight = 0.6f * getHeight();
    private GlyphString numEnlistedGlyph;
    private final float numEnlistedYOffset = -0.05f * getHeight();
    
    private final GlyphStringFactory glyphStringFactory;
    private final ArmySelectionRenderer renderer;
    private final SimpleTexturedShader shader;
    
    @Inject
    protected EnlistedUnitEntry(@Named("SimpleArrowTexture") final int arrowTexture,
                                @Named("UnitEntryBackgroundTexture") final int backgroundTexture,
                                final Device device,
                                final GlyphStringFactory glyphStringFactory,
                                @Named("EntryOrbTexture") final int orbTexture,
                                final SimpleTexturedShader shader,
                                final ArmySelectionRenderer renderer,
                                @Assisted("unit") final Unit unit,
                                @Assisted("height") final float height,
                                @Assisted("width") final float width) {
        super(backgroundTexture, device, glyphStringFactory, height, orbTexture, shader, unit, width);
        
        this.arrowTexture = arrowTexture;
        this.enlistedLabel = glyphStringFactory.create("ENLISTED", getHeight() * 0.35f);
        this.glyphStringFactory = glyphStringFactory;
        this.numEnlistedGlyph = glyphStringFactory.create("0", numEnlistedHeight);
        this.renderer = renderer;
        this.shader = shader;
    }
    public interface EnlistedUnitEntryFactory {
        EnlistedUnitEntry create(@Assisted("unit") final Unit unit,
                                 @Assisted("height") final float height,
                                 @Assisted("width") final float width);
    }
    
    public final int getNumEnlisted() { return numEnlisted; }
    
    /** {@inheritDocs} */
    @Override
    public boolean handleClick(final NormalizedPoint2D clickLocation,
                               final NormalizedPoint2D myPosition) {
        // Calculate the increment arrow's position
        float arrowX = myPosition.getX() + getWidth() / 2.0f;
        arrowX -= arrowSize / 2.0f + arrowXOffset;
        float incrementArrowY = myPosition.getY() + arrowSize / 2.0f + arrowYOffset;
        
        // Calculate the decrement arrow's position
        float decrementArrowY = myPosition.getY() - arrowSize / 2.0f - arrowYOffset;
        
        // Check if the increment arrow was touched
        if (InputHelper.isTouched(new Point2D(arrowX, incrementArrowY), arrowSize, arrowSize, clickLocation) &&
            (getEnlistCost() <= renderer.getRecruitPoints())) {
            renderer.spendRecruitPoints(getEnlistCost());
            numEnlisted++;
            numEnlistedGlyph = glyphStringFactory.create(Integer.toString(numEnlisted), numEnlistedHeight);
            return true;
        }
        
        // Check if the decrement arrow was touched
        if (InputHelper.isTouched(new Point2D(arrowX, decrementArrowY), arrowSize, arrowSize, clickLocation) &&
            (numEnlisted > 0)) {
            renderer.recoverRecruitPoints(getEnlistCost());
            numEnlisted--;
            numEnlistedGlyph = glyphStringFactory.create(Integer.toString(numEnlisted), numEnlistedHeight);
            return true;
        }
        
        return false;
    }
    
    @Override
    public void render(final MVP mvp) {
        final float backgroundRightEdge = 0.5f * getWidth();
        
        super.render(mvp);
        shader.activate();
        
        // Render the "ENLISTED" label above the number of units enlisted
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        float xPosition = backgroundRightEdge - enlistedLabel.getWidth() / 2.0f - enlistedLabelXOffset;
        float yPosition = enlistedLabelYOffset;
        Matrix.translateM(model, Constants.NO_OFFSET, xPosition, yPosition, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        enlistedLabel.render(mvp);
        mvp.pop(MVP.Type.MODEL);
        
        // Render the number of the unit enlisted
        model = mvp.peekCopy(MVP.Type.MODEL);
        yPosition = numEnlistedYOffset;
        Matrix.translateM(model, Constants.NO_OFFSET, xPosition, yPosition, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        numEnlistedGlyph.render(mvp);
        mvp.pop(MVP.Type.MODEL);
        
        // Render the increment arrow
        xPosition = backgroundRightEdge - arrowSize / 2.0f - arrowXOffset;
        if (getEnlistCost() <= renderer.getRecruitPoints()) {
            model = mvp.peekCopy(MVP.Type.MODEL);
            yPosition = arrowSize / 2.0f + arrowYOffset;
            Matrix.translateM(model, Constants.NO_OFFSET, xPosition, yPosition, 0.0f);
            Matrix.scaleM(model, Constants.NO_OFFSET, arrowSize, arrowSize, 1.0f);
            shader.setMVPMatrix(mvp.collapseM(model));
            shader.setTexture(arrowTexture);
            shader.draw();
        }
        
        // Render the decrement arrow
        if (numEnlisted > 0) {
            model = mvp.peekCopy(MVP.Type.MODEL);
            yPosition = -arrowSize / 2.0f - arrowYOffset;
            Matrix.translateM(model, Constants.NO_OFFSET, xPosition, yPosition, 0.0f);
            Matrix.scaleM(model, Constants.NO_OFFSET, arrowSize, arrowSize, 1.0f);
            Matrix.rotateM(model, Constants.NO_OFFSET, 180.0f, 0.0f, 0.0f, 1.0f);
            shader.setMVPMatrix(mvp.collapseM(model));
            shader.setTexture(arrowTexture);
            shader.draw();
        }
    }
}
