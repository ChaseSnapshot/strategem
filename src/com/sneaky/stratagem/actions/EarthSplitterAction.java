package com.sneaky.stratagem.actions;

import scatcat.general.points.GridPoint2D;
import scatcat.graphics.MVP;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.sneaky.stratagem.actions.EarthSpike.EarthSpikeFactory;
import com.sneaky.stratagem.actions.threats.LinearAttackPattern.LinearAttackPatternFactory;
import com.sneaky.stratagem.match.Match;
import com.sneaky.stratagem.units.Unit;

public class EarthSplitterAction extends Action {
    private static final int actionPointCost = 1;
    
    private final Unit actor;
    
    private static final int chargePointCost = 5;
    
    private final Injector injector;
    
    private GridPoint2D splitVector;
    
    @Inject
    protected EarthSplitterAction(@Assisted final Unit actor,
                                  final Injector injector,
                                  final LinearAttackPatternFactory threatRangeFactory) {
        super(actionPointCost, actor, chargePointCost, injector);
        
        this.actor = actor;
        this.injector = injector;
        
        setName("Earth Splitter");
        setThreatRange(threatRangeFactory.create(this));
    }
    
    public interface EarthSplitterActionFactory {
        EarthSplitterAction create(Unit actor);
    }
    
    @Override
    public void render(MVP mvp) { throw new UnsupportedOperationException(); }

    @Override
    public int getRange() {
        return 10; // Large enough to ensure all tiles in the line are threatened
    }

    @Override
    public void performThreatenedAction(final Unit actingUnit,
                                        final GridPoint2D sourceTile, 
                                        final GridPoint2D targetTile) {
        splitVector = ActionHelper.calcDirectionVector(sourceTile, targetTile);
        
        // Determine the first tile to be hit by the action
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        final GridPoint2D nextTilePos = sourceTile.plus(splitVector);
        final Optional<Tile> nextTile = battlefield.getTile(nextTilePos);
        
        // If the next exists
        if (nextTile.isPresent()) {
            // Start a spike in it
            EarthSpikeFactory spikeFactory = injector.getInstance(EarthSpikeFactory.class);
            nextTile.get().addEffect(spikeFactory.create(actor.getAttackDamage(), nextTilePos, splitVector));
            
            // Mark the unit as having acted and spend the action/charge points
            final Match match = injector.getInstance(Match.class);
            match.spendActionPoints(actionPointCost);
            actor.spendChargePoints(chargePointCost);
            actor.setHasActed(true);
        }
    }

    @Override
    public void updateState(int updatesPerSecond) { throw new UnsupportedOperationException(); }

    @Override
    public Executable clone() { throw new UnsupportedOperationException(); }
}
