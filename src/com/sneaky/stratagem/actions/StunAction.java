package com.sneaky.stratagem.actions;

import scatcat.effects.StatusMessageAnimation.StatusMessageAnimationFactory;
import scatcat.general.points.GridPoint2D;
import scatcat.graphics.Color;
import scatcat.graphics.MVP;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.sneaky.stratagem.actions.threats.RadialAttackPattern.RadialAttackPatternFactory;
import com.sneaky.stratagem.effects.StunEffect.StunEffectFactory;
import com.sneaky.stratagem.match.Match;
import com.sneaky.stratagem.units.Unit;

public class StunAction extends Action {
    private static final int actionPointCost = 1;
    
    private final Unit actor;
    
    private static final int chargePointCost = 2;
    
    private static final int duration = 2; 
    
    private final Injector injector;
    
    private static final int range = 2;
    
    @Inject
    protected StunAction(@Assisted Unit actor,
                         Injector injector,
                         RadialAttackPatternFactory threatRangeFactory) {
        super(actionPointCost, actor, chargePointCost, injector);
        
        this.actor = actor;
        this.injector = injector;
        
        setName("Stun");
        setThreatRange(threatRangeFactory.create(this));
    }
    
    public interface StunActionFactory {
        StunAction create(Unit actor);
    }

    @Override
    public void render(MVP mvp) { throw new UnsupportedOperationException(); }

    @Override
    public int getRange() { return range; }

    @Override
    public void performThreatenedAction(Unit actingUnit, GridPoint2D sourceTile, GridPoint2D targetTile) {
        Battlefield battlefield = injector.getInstance(Battlefield.class);
        
        // If the targeted tile has an occupant
        Tile tile = battlefield.getTile(targetTile).get();
        if (tile.hasOccupant()) {
            Unit target = tile.getOccupant();
            
            // Apply the stun
            StunEffectFactory stunEffectFactory = injector.getInstance(StunEffectFactory.class);
            target.addEffect(stunEffectFactory.create(duration, target));
            StatusMessageAnimationFactory statusFactory = injector.getInstance(StatusMessageAnimationFactory.class);
            target.addEffect(statusFactory.create(Color.RED, "STUNNED!"));
            
            // Spend the charge points required to stun the target
            actor.spendChargePoints(chargePointCost);
            
            // Spend the action points required to stun the target
            Match match = injector.getInstance(Match.class);
            match.spendActionPoints(actionPointCost);
            
            // Set the unit as having acted
            actor.setHasActed(true);
        }
    }

    @Override
    public void updateState(int updatesPerSecond) { throw new UnsupportedOperationException(); }

    @Override
    public Executable clone() { throw new UnsupportedOperationException(); }
}
