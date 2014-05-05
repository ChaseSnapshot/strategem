package com.sneaky.stratagem.actions;

import scatcat.general.points.GridPoint2D;
import scatcat.graphics.MVP;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.threats.RadialAttackPattern.RadialAttackPatternFactory;
import com.sneaky.stratagem.match.Match;
import com.sneaky.stratagem.units.Unit;

public class AttackAction extends Action {
    private static final int actionPointCost = 1;
    
    private final Unit actor;
    
    private static final int chargePointCost = 0;
   
    private final Injector injector;
    
    @Inject
    protected AttackAction(@Assisted final Unit actor,
                           final RadialAttackPatternFactory attackRangeFactory,
                           @Named("AttackIcon") final int icon,
                           final Injector injector) {
        super(actionPointCost, actor, chargePointCost, injector);
        
        this.actor = actor;
        this.injector = injector;
        
        setIconTexture(icon);
        setName("Attack");
        setThreatRange(attackRangeFactory.create(this));
    }
    
    public interface AttackActionFactory {
        AttackAction create(Unit target);
    }
    
    @Override
    public AttackAction clone() {
        return injector.getInstance(AttackAction.class);
    }

    @Override
    public int getRange() { return actor.getAttackRange(); }
    
    @Override
    public void performThreatenedAction(Unit actingUnit, GridPoint2D sourceTile, GridPoint2D targetTile) {
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        final Match match = injector.getInstance(Match.class);
        
        // If the tile has an occupant
        Tile tile = battlefield.getTile(targetTile).get();
        if (tile.hasOccupant()) {
            Unit target = tile.getOccupant();
            
            // If the occupant is on the enemy team
            if (target.getOwner() != actingUnit.getOwner()) {
                // Damage the target
                target.takeDamage(actingUnit.getAttackDamage());
                
                // Spend the action points for the attack
                match.spendActionPoints(actingUnit.getApCostOfAttack());
                
                // Set the attacking unit as having attacked
                actingUnit.setHasActed(true);
            }
        }
    }

    @Override
    public void render(MVP mvp) {
        throw new RuntimeException();
    }

    @Override
    public void updateState(int updatesPerSecond) {
        throw new RuntimeException();
    }
}
