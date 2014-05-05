package com.sneaky.stratagem.actions;

import scatcat.general.points.GridPoint2D;
import scatcat.general.points.Point2D;
import scatcat.graphics.MVP;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.sneaky.stratagem.actions.threats.LinearChargePattern.LinearChargePatternFactory;
import com.sneaky.stratagem.flow.GameFlowController;
import com.sneaky.stratagem.match.Match;
import com.sneaky.stratagem.units.Unit;

public class ChargeAction extends Action {
    private static final int actionPointCost = 1;
    
    private final Unit actor;
    
    private GridPoint2D actorPos;
    
    private static final int chargePointCost = 2;
    
    private GridPoint2D chargeVector;
    
    private final Injector injector;
    
    private boolean isRebounding = false;
    
    private boolean isMovingToNextTile = false;
    
    private GridPoint2D startingPos;
    
    @Inject
    protected ChargeAction(@Assisted final Unit actor,
                           final Injector injector,
                           final LinearChargePatternFactory threatRangeFactory) {
        super(actionPointCost, actor, chargePointCost, injector);
    
        this.actor = actor;
        this.injector = injector;
        
        setName("Charge");
        setThreatRange(threatRangeFactory.create(this));
    }
    
    public interface ChargeActionFactory {
        ChargeAction create(Unit actor);
    }
    
    @Override
    public boolean isExecutable() {
        final Match match = injector.getInstance(Match.class);
        
        return (!actor.isStunned() &&
                actor.canMove() &&
                (match.getCurrentActionPoints() >= actor.getApCostOfMovement()) &&
                (actor.getChargePoints() >= chargePointCost));
    }

    @Override
    public int getRange() {
        final int extendedRange = 3;
        
        return actor.getMovementRange() + extendedRange;
    }

    @Override
    public void performThreatenedAction(final Unit actingUnit,
                                        final GridPoint2D sourceTile, 
                                        final GridPoint2D targetTile) {
        actorPos = sourceTile;
        startingPos = sourceTile;
        
        chargeVector = ActionHelper.calcDirectionVector(sourceTile, targetTile);
        
        // Check if the charger is moving into the next tile
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        isMovingToNextTile = ActionHelper.checkIfNextTileIsOccupiable(
                battlefield, sourceTile.plus(chargeVector), getRange(), startingPos);
        isRebounding = false;
        
        // Mark the unit has having acted and spend the action points
        final Match match = injector.getInstance(Match.class);
        match.spendActionPoints(actionPointCost);
        actor.spendChargePoints(chargePointCost);
        actor.setHasMoved(true);
        
        // Start moving the unit
        GameFlowController gameFlow = injector.getInstance(GameFlowController.class);
        gameFlow.addUpdatable(this);
    }

    @Override
    public void render(MVP mvp) { throw new RuntimeException(); }
    
    @Override
    public void updateState(int updatesPerSecond) {
        final float tilesChargedPerSecond = 5.0f;
        final Point2D tileOffset = actor.getTileOffset();
        
        // If the charger has slid into the next tile, update its position
        if ((Math.abs(tileOffset.getX()) >= 0.5f) || (Math.abs(tileOffset.getY()) >= 0.5f)) {
            final Battlefield battlefield = injector.getInstance(Battlefield.class);
            
            // Remove the charger from its current tile
            Tile currentTile = battlefield.getTile(actorPos).get();
            currentTile.setOccupant(null);
            
            // Remove the "selected" ring from the current tile
            currentTile.setIsSelected(false);
            
            // Place it in the tile its charged into
            actorPos = actorPos.plus(chargeVector);
            Tile newTile = battlefield.getTile(actorPos).get();
            newTile.setOccupant(actor);
            
            // Set te moved to tile as selected
            newTile.setIsSelected(true);
            battlefield.setSelectedTile(Optional.of(actorPos));
            
            // Invert the tile offset
            tileOffset.setX(-tileOffset.getX());
            tileOffset.setY(-tileOffset.getY());
            
            // Determine if the next tile is occupiable
            isMovingToNextTile = ActionHelper.checkIfNextTileIsOccupiable(
                    battlefield, actorPos.plus(chargeVector), getRange(), startingPos);
        }
        // If the unit is rebounding from charging into an obstacle
        if (!isMovingToNextTile && isRebounding) {
            // If the bashed unit is finished rebounding
            if (((chargeVector.getRow() > 0.0f) && (tileOffset.getX() <= 0.0f)) ||
                ((chargeVector.getRow() < 0.0f) && (tileOffset.getX() >= 0.0f)) ||
                ((chargeVector.getColumn() > 0.0f) && (tileOffset.getY() <= 0.0f)) ||
                ((chargeVector.getColumn() < 0.0f) && (tileOffset.getY() >= 0.0f))) {
                // Zero out any hanging offset
                tileOffset.setX(0.0f);
                tileOffset.setY(0.0f);
                
                // Finish the charge action
                GameFlowController gameFlow = injector.getInstance(GameFlowController.class);
                gameFlow.removeUpdatable(this);
            }
            // If the charged unit is still rebounding
            else {
                final Point2D reboundVector = new Point2D();
                reboundVector.setX(-chargeVector.getRow());
                reboundVector.setY(-chargeVector.getColumn());
                
                tileOffset.addToX(-chargeVector.getRow() * tilesChargedPerSecond / updatesPerSecond);
                tileOffset.addToY(-chargeVector.getColumn() * tilesChargedPerSecond / updatesPerSecond);
            }
        }
        // If the unit can't move to the next tile
        else if (!isMovingToNextTile) {
            final float reboundStart = 0.2f;
            
            // If the unit has passed the small movement amount allocated when reaching the end of the slide
            if (((chargeVector.getRow() > 0.0f) && (tileOffset.getX() >= reboundStart)) ||
                ((chargeVector.getRow() < 0.0f) && (tileOffset.getX() <= -reboundStart)) ||
                ((chargeVector.getColumn() > 0.0f) && (tileOffset.getY() >= reboundStart)) ||
                ((chargeVector.getColumn() < 0.0f) && (tileOffset.getY() <= -reboundStart))) {
                // Start the rebounding portion of the animation
                isRebounding = true;
                
                // If the unoccupiable tile has an enemy unit, damage it
                final Battlefield battlefield = injector.getInstance(Battlefield.class);
                final Optional<Tile> unoccupiableTile = battlefield.getTile(actorPos.plus(chargeVector));
                if (unoccupiableTile.isPresent() && unoccupiableTile.get().hasOccupant() &&
                    unoccupiableTile.get().getOccupant().getOwner() != actor.getOwner()) {
                    // Apply the impact damage
                    final Unit enemy = unoccupiableTile.get().getOccupant();
                    enemy.takeDamage(actor.getAttackDamage() / 2);
                }
            }
            // If the unit hasn't started rebounding yet
            else {
                tileOffset.addToX(chargeVector.getRow() * tilesChargedPerSecond / updatesPerSecond);
                tileOffset.addToY(chargeVector.getColumn() * tilesChargedPerSecond / updatesPerSecond);
            }
        } else {  
            tileOffset.addToX(chargeVector.getRow() * tilesChargedPerSecond / updatesPerSecond);
            tileOffset.addToY(chargeVector.getColumn() * tilesChargedPerSecond / updatesPerSecond);
        }
    }

    @Override
    public Executable clone() { throw new UnsupportedOperationException(); }
}
