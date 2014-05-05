package com.sneaky.stratagem.armyselection;

import android.opengl.Matrix;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sneaky.stratagem.graphics.textures.UnitTexturePack;
import com.sneaky.stratagem.units.Unit;

import scatcat.general.Constants;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.glyphs.GlyphString;
import scatcat.graphics.glyphs.GlyphString.GlyphStringFactory;
import scatcat.graphics.shaders.SimpleTexturedShader;

/**
 * Widget that displayed detailed information about a unit.
 * 
 * @author R. Matt McCann
 */
public class DetailedUnitView implements RenderableMVP {
    /** Background panel settings. */
    private final float backgroundHeight = 0.9f;
    private final int backgroundTexture;
    private final float backgroundWidth = 0.9f;
    
    private final float indentSpacing = 0.05f * backgroundWidth;
    private final float leftEdge = -0.45f * backgroundWidth;
    private final float lineHeight = 0.05f * backgroundHeight;
    private final float lineSpacing = 0.02f * backgroundHeight;
    private final float rightEdge = 0.45f * backgroundWidth;
    private final float sectionSpacing = lineSpacing * 2.0f;
    private final float topEdge = 0.45f * backgroundHeight;
    private final float valueCol = -0.1f * backgroundWidth;
    
    /** Name label settings. */
    private final GlyphString nameLabel;
    private final float nameLabelXPos;
    private final float nameLabelYPos = topEdge - lineHeight / 2.0f;
    
    /** Unit name settings. */
    private Optional<GlyphString> unitName = Optional.absent();
    private final float unitNameYPos = nameLabelYPos;
            
    /** Cost To Enlist label settings. */
    private final GlyphString costLabel;
    private final float costLabelXPos;
    private final float costLabelYPos = nameLabelYPos - lineHeight - lineSpacing; 
  
    /** Unit's cost to enlist settings. */
    private Optional<GlyphString> unitCost = Optional.absent();
    private final float unitCostYPos = costLabelYPos;
    
    /** Attack label settings. */
    private final GlyphString attackLabel;
    private final float attackLabelXPos;
    private final float attackLabelYPos = costLabelYPos - lineHeight - sectionSpacing;
    
    /** Attack cost label settings. */
    private final GlyphString attackCostLabel;
    private final float attackCostLabelXPos;
    private final float attackCostLabelYPos = attackLabelYPos - lineHeight - lineSpacing;
    
    /** Unit attack cost settings. */
    private Optional<GlyphString> unitAttackCost = Optional.absent();
    private final float unitAttackCostYPos = attackCostLabelYPos;
    
    /** Attack range label settings. */
    private final GlyphString attackRangeLabel;
    private final float attackRangeLabelXPos;
    private final float attackRangeLabelYPos = attackCostLabelYPos - lineHeight - lineSpacing;
    
    /** Unit attack range settings. */
    private Optional<GlyphString> unitAttackRange = Optional.absent();
    private final float unitAttackRangeYPos = attackRangeLabelYPos;
    
    /** Attack type label settings. */
    private final GlyphString attackTypeLabel;
    private final float attackTypeLabelXPos;
    private final float attackTypeLabelYPos = attackRangeLabelYPos - lineHeight - lineSpacing;
    
    /** Unit attack type settings. */
    private Optional<GlyphString> unitAttackType = Optional.absent();
    private final float unitAttackTypeYPos = attackTypeLabelYPos;
    
    /** Movement label settings. */
    private final GlyphString moveLabel;
    private final float moveLabelXPos;
    private final float moveLabelYPos = attackTypeLabelYPos - lineHeight - lineSpacing;
    
    /** Movement cost label settings. */
    private final GlyphString moveCostLabel;
    private final float moveCostLabelXPos;
    private final float moveCostLabelYPos = moveLabelYPos - lineHeight - lineSpacing;
    
    /** Unit movement cost settings. */
    private Optional<GlyphString> unitMoveCost = Optional.absent();
    private final float unitMoveCostYPos = moveCostLabelYPos;
    
    /** Movement range label settings. */
    private final GlyphString moveRangeLabel;
    private final float moveRangeLabelXPos;
    private final float moveRangeLabelYPos = moveCostLabelYPos - lineHeight - lineSpacing;
    
    /** Unit movement range settings. */
    private Optional<GlyphString> unitMoveRange = Optional.absent();
    private final float unitMoveRangeYPos = moveRangeLabelYPos;
    
    /** Ability label settings. */
    private final GlyphString abilityLabel;
    private final float abilityLabelXPos;
    private final float abilityLabelYPos = moveRangeLabelYPos - lineHeight - sectionSpacing;
    
    /** Ability name label settings. */
    private final GlyphString abilityNameLabel;
    private final float abilityNameLabelXPos;
    private final float abilityNameLabelYPos = abilityLabelYPos - lineHeight - lineSpacing;
    
    /** Unit ability name settings. */
    private Optional<GlyphString> unitAbilityName = Optional.absent();
    private final float unitAbilityNameYPos = abilityNameLabelYPos;
    
    /** Ability charge label settings. */
    private final GlyphString abilityChargeLabel;
    private final float abilityChargeLabelXPos;
    private final float abilityChargeLabelYPos = abilityNameLabelYPos - lineHeight - lineSpacing;
   
    /** Unit ability charge settings. */
    private Optional<GlyphString> unitAbilityCharge = Optional.absent();
    private final float unitAbilityChargeYPos = abilityChargeLabelYPos;
    
    /** Unit views settings. */
    private Optional<UnitTexturePack> unitViews = Optional.absent();
    private final float unitViewsSize = 0.9f * backgroundHeight;
    private final float unitViewsXPos = rightEdge - unitViewsSize / 2.0f;
    private final float unitViewsYPos = 0.0f;
    
    /** The unit to show a detailed view of. */
    private Optional<Unit> target = Optional.absent();
    
    /** Shader used to render me. */
    private final GlyphStringFactory glyphStringFactory;
    private final SimpleTexturedShader shader;
    
    /** Injection constructor. */
    @Inject
    protected DetailedUnitView(@Named("DetailedUnitViewBackgroundTexture") final int backgroundTexture,
                               final GlyphStringFactory glyphStringFactory,
                               final SimpleTexturedShader shader) {
        this.backgroundTexture = backgroundTexture;
        this.glyphStringFactory = glyphStringFactory;
        this.shader = shader;
        
        this.abilityChargeLabel = glyphStringFactory.create("Charged By:", lineHeight);
        this.abilityChargeLabelXPos = leftEdge + indentSpacing + abilityChargeLabel.getWidth() / 2.0f;
        this.abilityLabel = glyphStringFactory.create("Ability", lineHeight);
        this.abilityLabelXPos = leftEdge + abilityLabel.getWidth() / 2.0f;
        this.abilityNameLabel = glyphStringFactory.create("Name:", lineHeight);
        this.abilityNameLabelXPos = leftEdge + indentSpacing + abilityNameLabel.getWidth() / 2.0f;
        this.attackCostLabel = glyphStringFactory.create("Cost:", lineHeight);
        this.attackCostLabelXPos = leftEdge + indentSpacing + attackCostLabel.getWidth() / 2.0f;
        this.attackLabel = glyphStringFactory.create("Attack", lineHeight);
        this.attackLabelXPos = leftEdge + attackLabel.getWidth() / 2.0f;
        this.attackRangeLabel = glyphStringFactory.create("Range:", lineHeight);
        this.attackRangeLabelXPos = leftEdge + indentSpacing + attackRangeLabel.getWidth() / 2.0f;
        this.attackTypeLabel = glyphStringFactory.create("Type:", lineHeight);
        this.attackTypeLabelXPos = leftEdge + indentSpacing + attackTypeLabel.getWidth() / 2.0f;
        this.costLabel = glyphStringFactory.create("Cost:", lineHeight);
        this.costLabelXPos = leftEdge + costLabel.getWidth() / 2.0f;
        this.moveCostLabel = glyphStringFactory.create("Cost:", lineHeight);
        this.moveCostLabelXPos = leftEdge + indentSpacing + moveCostLabel.getWidth() / 2.0f;
        this.moveLabel = glyphStringFactory.create("Movement", lineHeight);
        this.moveLabelXPos = leftEdge + moveLabel.getWidth() / 2.0f;
        this.moveRangeLabel = glyphStringFactory.create("Range:", lineHeight);
        this.moveRangeLabelXPos = leftEdge + indentSpacing + moveRangeLabel.getWidth() / 2.0f;
        this.nameLabel = glyphStringFactory.create("Name:", lineHeight);
        this.nameLabelXPos = leftEdge + nameLabel.getWidth() / 2.0f;
        
    }
    
    /** {@inheritDoc} */
    @Override
    public void render(final MVP mvp) {
        checkState(target.isPresent(), "Expected targeted unit for detailed viewing to be set!");
        
        shader.activate();
        
        // Render the background panel
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.scaleM(model, Constants.NO_OFFSET, backgroundWidth, backgroundHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(backgroundTexture);
        shader.draw();
        
        renderText(mvp, nameLabel, nameLabelXPos, nameLabelYPos);
        renderText(mvp, unitName.get(), valueCol - unitName.get().getWidth() / 2.0f, unitNameYPos);
        
        renderText(mvp, costLabel, costLabelXPos, costLabelYPos);
        renderText(mvp, unitCost.get(), valueCol - unitCost.get().getWidth() / 2.0f, unitCostYPos);
    
        renderText(mvp, attackLabel, attackLabelXPos, attackLabelYPos);
        
        renderText(mvp, attackCostLabel, attackCostLabelXPos, attackCostLabelYPos);
        renderText(mvp, unitAttackCost.get(), valueCol - unitAttackCost.get().getWidth() / 2.0f, unitAttackCostYPos);
        
        renderText(mvp, attackRangeLabel, attackRangeLabelXPos, attackRangeLabelYPos);
        renderText(mvp, unitAttackRange.get(), valueCol - unitAttackRange.get().getWidth() / 2.0f, unitAttackRangeYPos);
        
        renderText(mvp, attackTypeLabel, attackTypeLabelXPos, attackTypeLabelYPos);
        renderText(mvp, unitAttackType.get(),valueCol - unitAttackType.get().getWidth() / 2.0f, unitAttackTypeYPos);
        
        renderText(mvp, moveLabel, moveLabelXPos, moveLabelYPos);
        
        renderText(mvp, moveCostLabel, moveCostLabelXPos, moveCostLabelYPos);
        renderText(mvp, unitMoveCost.get(), valueCol - unitMoveCost.get().getWidth() / 2.0f, unitMoveCostYPos);
        
        renderText(mvp, moveRangeLabel, moveRangeLabelXPos, moveRangeLabelYPos);
        renderText(mvp, unitMoveRange.get(), valueCol - unitMoveRange.get().getWidth() / 2.0f, unitMoveRangeYPos);
        
        renderText(mvp, abilityLabel, abilityLabelXPos, abilityLabelYPos);
        
        renderText(mvp, abilityNameLabel, abilityNameLabelXPos, abilityNameLabelYPos);
        renderText(mvp, unitAbilityName.get(), valueCol - unitAbilityName.get().getWidth() / 2.0f, unitAbilityNameYPos);
        
        renderText(mvp, abilityChargeLabel, abilityChargeLabelXPos, abilityChargeLabelYPos);
        renderText(mvp, unitAbilityCharge.get(), valueCol - unitAbilityCharge.get().getWidth() / 2.0f, unitAbilityChargeYPos);
    
        model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, unitViewsXPos, unitViewsYPos, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, unitViewsSize, unitViewsSize, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(unitViews.get().getEast());
        shader.draw();
    }
    
    private void renderText(final MVP mvp, final GlyphString text, final float xPos, final float yPos) {
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, xPos, yPos, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        text.render(mvp);
        mvp.pop(MVP.Type.MODEL);
    }
    
    public void setTarget(final Unit target) {
        checkArgument(target != null, "Target must not be null!");
        
        this.target = Optional.of(target);
        this.unitName = Optional.of(glyphStringFactory.create(target.getName(), lineHeight));
        this.unitCost = 
                Optional.of(glyphStringFactory.create(Integer.toString(target.getEnlistCost()), lineHeight));
        this.unitAttackCost =
                Optional.of(glyphStringFactory.create(Integer.toString(target.getApCostOfAttack()), lineHeight));
        this.unitAttackRange = 
                Optional.of(glyphStringFactory.create(Integer.toString(target.getAttackRange()), lineHeight));
        //this.unitAttackType =
        //        Optional.of(glyphStringFactory.create(target.getAttackType().toString(), lineHeight));
        this.unitMoveCost =
                Optional.of(glyphStringFactory.create(Integer.toString(target.getApCostOfMovement()), lineHeight));
        this.unitMoveRange =
                Optional.of(glyphStringFactory.create(Integer.toString(target.getMovementRange()), lineHeight));
        //this.unitAbilityName =
        //        Optional.of(glyphStringFactory.create(target.getAbilityName(), lineHeight));
        this.unitAbilityCharge =
                Optional.of(glyphStringFactory.create(target.getAbilityChargeType().toString(), lineHeight));
        //this.unitViews = Optional.of(target.getTextureHandles());
    }
}
