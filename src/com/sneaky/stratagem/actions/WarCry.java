package com.sneaky.stratagem.actions;

import java.util.HashSet;
import java.util.Set;

import scatcat.general.points.GridPoint2D;
import scatcat.graphics.MVP;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.sneaky.stratagem.actions.threats.RadialBuffPattern.RadialBuffPatternFactory;
import com.sneaky.stratagem.effects.MoveBuff.MoveBuffFactory;
import com.sneaky.stratagem.units.Unit;

public class WarCry extends Action {
    private static final int actionPointCost = 1;
    
    private final Unit actor;
    
    private static final int chargePointCost = 2;
    
    private final Injector injector;
    
    @Inject
    protected WarCry(@Assisted final Unit actor,
                     final Injector injector,
                     final RadialBuffPatternFactory threatFactory) {
        super(actionPointCost, actor, chargePointCost, injector);
        
        this.actor = actor;
        this.injector = injector;
        
        setName("War Cry");
        setThreatRange(threatFactory.create(this));
    }
    
    public interface WarCryFactory {
        WarCry create(Unit actor);
    }
    
    @Override
    public void render(MVP mvp) { throw new UnsupportedOperationException(); }

    @Override
    public int getRange() { return 2; }

    @Override
    public void performThreatenedAction(final Unit actingUnit,
                                        final GridPoint2D sourceTile, 
                                        final GridPoint2D targetTile) {
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        
        // Get all of the tiles in range of the buff
        final Set<GridPoint2D> tilesInRange = new HashSet<GridPoint2D>();
        getThreatRange().populateThreatenedTiles(actingUnit, sourceTile, tilesInRange);
        
        // For each tile in range of the buff
        for (GridPoint2D tilePos : tilesInRange) {
            final Optional<Tile> tile = battlefield.getTile(tilePos);
            
            // If the tile exists and it contains a friendly unit
            if (tile.isPresent() && tile.get().hasOccupant() &&
                tile.get().getOccupant().getOwner() == actor.getOwner()) {
                Unit unit = tile.get().getOccupant();
                
                // Heal the unit
                final int amountHealed = 2;
                unit.gainHealth(amountHealed);
                
                // Temporarily increase the units movement speed
                MoveBuffFactory buffFactory = injector.getInstance(MoveBuffFactory.class);
                final int moveBuffAmount = 1;
                final int moveBuffDuration = 2;
                buffFactory.create(moveBuffAmount, moveBuffDuration, unit);
            }
        }
    }

    @Override
    public void updateState(int updatesPerSecond) { throw new UnsupportedOperationException(); }

    @Override
    public Executable clone() { throw new UnsupportedOperationException(); }
}
