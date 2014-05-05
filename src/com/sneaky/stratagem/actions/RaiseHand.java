package com.sneaky.stratagem.actions;

import scatcat.general.points.GridPoint2D;
import scatcat.graphics.MVP;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.sneaky.stratagem.actions.HelpingHand.HelpingHandFactory;
import com.sneaky.stratagem.actions.threats.RadialAttackPattern.RadialAttackPatternFactory;
import com.sneaky.stratagem.match.Match;
import com.sneaky.stratagem.units.Unit;

public class RaiseHand extends Action {
    private static final int actionPointCost = 1;
    
    private final Unit actor;
    
    private static final int chargePointCost = 3;
    
    private final Injector injector;
    
    private final int range = 3;
    
    @Inject
    protected RaiseHand(@Assisted final Unit actor,
                        final Injector injector,
                        final RadialAttackPatternFactory threatRangeFactory) {
        super(actionPointCost, actor, chargePointCost, injector);
        
        this.actor = actor;
        this.injector = injector;
        
        setName("Lend A Hand");
        setThreatRange(threatRangeFactory.create(this));
    }
    
    public interface RaiseHandFactory {
        RaiseHand create(Unit actor);
    }
    
    @Override
    public void render(MVP mvp) { throw new UnsupportedOperationException(); }

    @Override
    public int getRange() { return range; }

    @Override
    public void performThreatenedAction(final Unit actingUnit,
                                        final GridPoint2D sourceTilePos, 
                                        final GridPoint2D targetTilePos) {
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        final Tile targetTile = battlefield.getTile(targetTilePos).get();
        
        // If the targeted tile has an occupant
        if (targetTile.hasOccupant()) {
            final Unit targetUnit = targetTile.getOccupant();
            
            // Raise a hand under the occupant
            HelpingHandFactory helpingHandFactory = injector.getInstance(HelpingHandFactory.class);
            HelpingHand helpingHand = helpingHandFactory.create(actingUnit.getOwner(), targetUnit);
            helpingHand.init();
            
            // Pay the action and charge point cost of the action
            final Match match = injector.getInstance(Match.class);
            match.spendActionPoints(actionPointCost);
            actor.spendChargePoints(chargePointCost);
        }
    }

    @Override
    public void updateState(int updatesPerSecond) { throw new UnsupportedOperationException(); }
    
    @Override
    public Executable clone() { throw new UnsupportedOperationException(); }
}
