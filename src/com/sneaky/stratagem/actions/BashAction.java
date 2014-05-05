package com.sneaky.stratagem.actions;

import scatcat.general.points.GridPoint2D;
import scatcat.general.points.Point2D;
import scatcat.graphics.MVP;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.sneaky.stratagem.actions.threats.RadialAttackPattern.RadialAttackPatternFactory;
import com.sneaky.stratagem.flow.GameFlowController;
import com.sneaky.stratagem.match.Match;
import com.sneaky.stratagem.units.Unit;

public class BashAction extends Action {
    private static final int actionPointCost = 1;
    
    private final Unit actor;

    private GridPoint2D bashVector;
    
    private static final int chargePointCost = 2;
    
    private final Injector injector;
    
    private boolean isRebounding = false;
    
    private final int range = 1;
    
    private Unit targetedUnit;
    
    private GridPoint2D targetedTilePos;
    
    private boolean isMovingToNextTile;
    
    @Inject
    protected BashAction(@Assisted final Unit actor,
                         final Injector injector,
                         RadialAttackPatternFactory threatRangeFactory) {
        super(actionPointCost, actor, chargePointCost, injector);
    
        this.actor = actor;
        this.injector = injector;
        
        setName("Bash");
        setThreatRange(threatRangeFactory.create(this));
    }
    
    public interface BashActionFactory {
        BashAction create(Unit actor);
    }
    
    @Override
    public void render(MVP mvp) { throw new UnsupportedOperationException(); }

    @Override
    public int getRange() { return range; }
    
    @Override
    public void performThreatenedAction(final Unit actingUnit,
                                        final GridPoint2D sourceTile, 
                                        final GridPoint2D targetTile) {
        Battlefield battlefield = injector.getInstance(Battlefield.class);
        
        // If the targeted tile has an occupant
        Tile tile = battlefield.getTile(targetTile).get();
        if (tile.hasOccupant()) {
            targetedUnit = tile.getOccupant();
            targetedTilePos = targetTile;
            
            // Apply the initial attack damage to the target
            targetedUnit.takeDamage(actingUnit.getAttackDamage());
            
            // Determine what direction the bash occurs in
            bashVector = new GridPoint2D();
            bashVector.setRow(targetTile.getRow() - sourceTile.getRow());
            bashVector.setColumn(targetTile.getColumn() - sourceTile.getColumn());
            
            // Initialize the animation state
            isMovingToNextTile = ActionHelper.checkIfNextTileIsOccupiable(battlefield, targetTile.plus(bashVector));
            isRebounding = false;
            
            // Mark the unit as having acted and spend the action points
            final Match match = injector.getInstance(Match.class);
            match.spendActionPoints(actionPointCost);
            actor.spendChargePoints(chargePointCost);
            actor.setHasActed(true);
            
            // Start moving the unit
            GameFlowController gameFlow = injector.getInstance(GameFlowController.class);
            gameFlow.addUpdatable(this);
        }
    }

    @Override
    public void updateState(int updatesPerSecond) {
        final float tilesSlidPerSecond = 10.0f;
        Point2D tileOffset = targetedUnit.getTileOffset();
        
        // If the unit has slid into the next tile, update its position
        if ((Math.abs(tileOffset.getX()) >= 0.5f) || (Math.abs(tileOffset.getY()) >= 0.5f)) {
            final Battlefield battlefield = injector.getInstance(Battlefield.class);
            
            // Remove the unit from its current tile
            Tile targetedTile = battlefield.getTile(targetedTilePos).get();
            targetedTile.setOccupant(null);
            
            // Place it in the tile its slide into
            targetedTilePos = targetedTilePos.plus(bashVector);
            Tile newTile = battlefield.getTile(targetedTilePos).get();
            newTile.setOccupant(targetedUnit);
            
            // Invert the tile offset
            tileOffset.setX(-tileOffset.getX());
            tileOffset.setY(-tileOffset.getY());
            
            // Determine if the next tile is occupiable
            isMovingToNextTile = ActionHelper.checkIfNextTileIsOccupiable(battlefield, targetedTilePos.plus(bashVector));
        }
        
        // If the unit is rebounding from bashing into an obstacle
        if (!isMovingToNextTile && isRebounding) {
            // If the bashed unit is finished rebounding
            if (((bashVector.getRow() > 0.0f) && (tileOffset.getX() <= 0.0f)) ||
                ((bashVector.getRow() < 0.0f) && (tileOffset.getX() >= 0.0f)) ||
                ((bashVector.getColumn() > 0.0f) && (tileOffset.getY() <= 0.0f)) ||
                ((bashVector.getColumn() < 0.0f) && (tileOffset.getY() >= 0.0f))) {
                // Zero out any hanging offset
                tileOffset.setX(0.0f);
                tileOffset.setY(0.0f);
                
                // Finish the bash action
                GameFlowController gameFlow = injector.getInstance(GameFlowController.class);
                gameFlow.removeUpdatable(this);
            }
            // If the bashed unit is still rebounding
            else {
                final Point2D reboundVector = new Point2D();
                reboundVector.setX(-bashVector.getRow());
                reboundVector.setY(-bashVector.getColumn());
                
                tileOffset.addToX(-bashVector.getRow() * tilesSlidPerSecond / updatesPerSecond);
                tileOffset.addToY(-bashVector.getColumn() * tilesSlidPerSecond / updatesPerSecond);
            }
        }
        // If the unit can't move to the next tile
        else if (!isMovingToNextTile) {
            final float reboundStart = 0.2f;
            
            // If the unit has passed the small movement amount allocated when reaching the end of the slide
            if (((bashVector.getRow() > 0.0f) && (tileOffset.getX() >= reboundStart)) ||
                ((bashVector.getRow() < 0.0f) && (tileOffset.getX() <= -reboundStart)) ||
                ((bashVector.getColumn() > 0.0f) && (tileOffset.getY() >= reboundStart)) ||
                ((bashVector.getColumn() < 0.0f) && (tileOffset.getY() <= -reboundStart))) {
                // Start the rebounding portion of the animation
                isRebounding = true;
                
                // Apply the impact damage
                targetedUnit.takeDamage(actor.getAttackDamage() / 2);
            }
            // If the unit hasn't started rebounding yet
            else {
                tileOffset.addToX(bashVector.getRow() * tilesSlidPerSecond / updatesPerSecond);
                tileOffset.addToY(bashVector.getColumn() * tilesSlidPerSecond / updatesPerSecond);
            }
        } else {  
            tileOffset.addToX(bashVector.getRow() * tilesSlidPerSecond / updatesPerSecond);
            tileOffset.addToY(bashVector.getColumn() * tilesSlidPerSecond / updatesPerSecond);
        }
    }

    @Override
    public Executable clone() { throw new UnsupportedOperationException(); }
}
