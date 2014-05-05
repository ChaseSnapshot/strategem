package com.sneaky.stratagem.actions;

import scatcat.general.Cleanable;
import scatcat.general.points.GridPoint2D;
import scatcat.graphics.MVP;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.sneaky.stratagem.actions.threats.RadialBuffPattern;
import com.sneaky.stratagem.actions.threats.RadialBuffPattern.RadialBuffPatternFactory;
import com.sneaky.stratagem.actions.threats.RadialMovementPattern;
import com.sneaky.stratagem.actions.threats.RadialMovementPattern.RadialMovementPatternFactory;
import com.sneaky.stratagem.match.Match;
import com.sneaky.stratagem.units.Unit;

public class TeleportAction extends Action {
    private static final int actionPointCost = 1;
    
    private final Unit actor;
    
    private static final int chargePointCost = 2;
    
    private final Injector injector;
    
    private boolean isTeleporting = false;
    
    private final RadialMovementPatternFactory movementRangeFactory;
    
    private static final int range = 3;
    
    private final RadialBuffPattern pickUnitPattern;
    
    @Inject
    protected TeleportAction(@Assisted Unit actor,
                             Injector injector,
                             RadialMovementPatternFactory movementRangeFactory,
                             RadialBuffPatternFactory threatRangeFactory) {
        super(actionPointCost, actor, chargePointCost, injector);
        
        this.actor = actor;
        this.injector = injector;
        this.movementRangeFactory = movementRangeFactory;
        
        setName("Teleport");
        
        // Set up the teleport pattern
        pickUnitPattern = threatRangeFactory.create(this);
        setThreatRange(pickUnitPattern);
    }
    
    public interface TeleportActionFactory {
        TeleportAction create(Unit actor);
    }
    
    @Override
    public int getRange() { return range; }
    
    @Override
    public void render(MVP mvp) { throw new UnsupportedOperationException(); }

    @Override
    public void performThreatenedAction(final Unit actingUnit,
                                        final GridPoint2D sourceTilePos, 
                                        final GridPoint2D targetTilePos) {
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        final Match       match = injector.getInstance(Match.class);
        final Tile        sourceTile = battlefield.getTile(sourceTilePos).get();
        final Tile        targetedTile = battlefield.getTile(targetTilePos).get();
        
        // If the to-be-teleported unit hasn't been selected yet
        if (!isTeleporting) {
            // If the selected tile doesn't contain a unit
            if (!targetedTile.hasOccupant()) { return; }
            
            // Clean up the pick unit threat highlights
            pickUnitPattern.cleanUp();
            
            // Override the clean up behavior so it doesn't erase the teleport pattern
            pickUnitPattern.setCleanUpFunction(new Cleanable() {
                @Override
                public void cleanUp() { /* Do nothing */  }    
            });
            
            // Set the targeted unit as selected so the teleport pattern will be applied around them
            battlefield.setSelectedTile(Optional.of(targetTilePos));
            sourceTile.setIsSelected(false);
            targetedTile.setIsSelected(true);
            
            // Apply the movement range for the teleport
            RadialMovementPattern teleportPattern = movementRangeFactory.create(this);
            teleportPattern.applyThreat();
            
            isTeleporting = true;
        }
        // If the unit to-be-teleported has already been selected
        else {
            final Unit teleportingUnit = sourceTile.getOccupant();
            
            // Remove the unit from its current tile
            sourceTile.setOccupant(null);
            
            // Remove the "selected" ring from the current tile
            sourceTile.setIsSelected(false);
            
            // Place the unit the tile it's teleported to
            targetedTile.setOccupant(teleportingUnit);
            
            // Set the moved to tile as selected
            targetedTile.setIsSelected(true);
            battlefield.setSelectedTile(Optional.of(targetTilePos));
            
            // Pay the action point cost of the teleport
            match.spendActionPoints(actionPointCost);
            
            // Pay the charge point cost of the teleport
            actor.spendChargePoints(chargePointCost);
            
            // Set the castor of teleport as having acted
            actor.setHasActed(true);
        }
    }

    @Override
    public void updateState(int updatesPerSecond) { throw new UnsupportedOperationException(); }

    @Override
    public Executable clone() { throw new UnsupportedOperationException(); }
}
