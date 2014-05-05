package com.sneaky.stratagem.actions;

import java.util.Set;

import scatcat.general.points.GridPoint2D;
import scatcat.graphics.MVP;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.sneaky.stratagem.actions.threats.ThreatPatternHelper;
import com.sneaky.stratagem.units.Unit;

public class ManaBatteryAction extends Action {
    private static final int actionPointCost = 1;
    
    private final Unit actor;
    
    private static final int chargePointCost = 2;
    
    private final Injector injector;
    
    @Inject
    protected ManaBatteryAction(@Assisted Unit actor,
                                Injector injector) {
        super(actionPointCost, actor, chargePointCost, injector);
        
        this.actor = actor;
        this.injector = injector;
        
        setName("Mana Battery");
    }
    
    public interface ManaBatteryActionFactory {
        ManaBatteryAction create(Unit actor);
    }
    
    @Override
    public void execute() { 
        Battlefield battlefield = injector.getInstance(Battlefield.class);
        
        // Get all of the adjacent tiles
        final int radius = 1;
        Set<Tile> tilesInRange = ThreatPatternHelper.getTilesInSquarePattern(battlefield, actor, radius);
        
        // For each tile in range of the mana battery effect
        for (Tile tile : tilesInRange) {
            // If the tile has an occupant and it isn't the caster of mana battery
            if (tile.hasOccupant() && (tile.getOccupant() != actor)) {
                // Grant the unit an extra charge point
                int chargePointsGained = 1;
                tile.getOccupant().gainChargePoints(chargePointsGained);
            }
        }
    }
    
    @Override
    public void render(MVP mvp) { throw new UnsupportedOperationException(); }

    @Override
    public int getRange() { throw new UnsupportedOperationException(); }

    @Override
    public void performThreatenedAction(Unit actingUnit, GridPoint2D sourceTile, GridPoint2D targetTile) {
        throw new UnsupportedOperationException(); }

    @Override
    public void updateState(int updatesPerSecond) { throw new UnsupportedOperationException(); }

    @Override
    public Executable clone() { throw new UnsupportedOperationException(); }
}
