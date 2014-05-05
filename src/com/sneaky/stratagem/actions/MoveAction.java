package com.sneaky.stratagem.actions;

import scatcat.general.points.GridPoint2D;
import scatcat.graphics.MVP;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.threats.RadialMovementPattern;
import com.sneaky.stratagem.actions.threats.RadialMovementPattern.RadialMovementPatternFactory;
import com.sneaky.stratagem.match.Match;
import com.sneaky.stratagem.units.Unit;

public class MoveAction extends Action {
    private static final int actionPointCost = 1;
    
    private final Unit actor;
    
    private static final int chargePointCost = 0;
    
    private final Injector injector;
    
    private final RadialMovementPattern movementRange;
    
    @Inject
    protected MoveAction(@Assisted final Unit actor,
                         @Named("MoveIcon") final int icon,
                         final Injector injector,
                         final RadialMovementPatternFactory movementRangeFactory
                         ) {
        super(actionPointCost, actor, chargePointCost, injector);
        
        this.actor = actor;
        this.injector = injector;
        this.movementRange = movementRangeFactory.create(this);
        
        setIconTexture(icon);
        setName("Move");
    }
    
    public interface MoveActionFactory { MoveAction create(Unit target); }
    
    @Override
    public MoveAction clone() { throw new UnsupportedOperationException(); }

    @Override
    public void execute() { movementRange.applyThreat(); }
    
    @Override
    public boolean isExecutable() {
        final Match match = injector.getInstance(Match.class);
        
        return (!actor.isStunned() &&
                actor.canMove() &&
                (match.getCurrentActionPoints() >= actor.getApCostOfMovement()));
    }

    @Override
    public void render(MVP mvp) { throw new UnsupportedOperationException(); }

    @Override
    public int getRange() { return actor.getMovementRange(); }

    @Override
    public void performThreatenedAction(final Unit actingUnit,
                                        final GridPoint2D sourceTilePos, 
                                        final GridPoint2D targetTilePos) {
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        final Match       match = injector.getInstance(Match.class);
        final Tile        sourceTile = battlefield.getTile(sourceTilePos).get();
        final Tile        targetTile = battlefield.getTile(targetTilePos).get();
        
        // Remove the unit from its current tile
        sourceTile.setOccupant(null);
        
        // Remove the "selected" ring from the current tile
        sourceTile.setIsSelected(false);
        
        // Place the unit in the tile it's moving to
        targetTile.setOccupant(actingUnit);
        
        // Set the moved to tile as selected
        targetTile.setIsSelected(true);
        battlefield.setSelectedTile(Optional.of(targetTilePos));
        
        // Pay the action point cost of the move
        match.spendActionPoints(actingUnit.getApCostOfMovement());
        
        // Set the acting unit as having moved
        actingUnit.setHasMoved(true);
    }

    @Override
    public void updateState(int updatesPerSecond) { throw new UnsupportedOperationException(); }
}
