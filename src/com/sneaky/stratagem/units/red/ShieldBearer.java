package com.sneaky.stratagem.units.red;

import java.util.ArrayList;
import java.util.List;

import scatcat.effects.StatusMessageAnimation.StatusMessageAnimationFactory;
import scatcat.general.points.Point3D;
import scatcat.graphics.Color;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.AttackAction.AttackActionFactory;
import com.sneaky.stratagem.actions.Executable;
import com.sneaky.stratagem.actions.MoveAction.MoveActionFactory;
import com.sneaky.stratagem.units.Unit;

public class ShieldBearer extends Unit {
    private final Injector injector;
    
    @Inject
    protected ShieldBearer(final Injector injector,
                           @Named("ShieldBearerFront") final int shieldBearerFront) {
        super(injector, shieldBearerFront, shieldBearerFront, shieldBearerFront, shieldBearerFront, shieldBearerFront);
        
        this.injector = injector;
        
        setName("ShieldBearer");
        setChargePoints(0);
        setApCostOfAttack(1);
        setApCostOfMovement(1);
        setAttackDamage(2);
        setAttackRange(1);
        setEnlistCost(2);
        setHealth(25);
        setMaxChargePoints(2);
        setMaxHealth(25);
        setMovementRange(1);
        setNumPassivelyAccruedChargePoints(0);
        setTextureOffset(new Point3D(0.0f, 0.45f, 0.05f));
        
        List<Executable> actions = new ArrayList<Executable>();
        
        // Add the attack action
        AttackActionFactory attackFactory = injector.getInstance(AttackActionFactory.class);
        if (attackFactory != null) {
            actions.add(attackFactory.create(this));
        }
        
        // Add the move action
        MoveActionFactory moveFactory = injector.getInstance(MoveActionFactory.class);
        if (moveFactory != null) {
            actions.add(moveFactory.create(this));
        }
        
        setActions(actions);
    }
    
    @Override
    public void takeDamage(final int damage) {
        // If the shield bearer is fully charged
        if (getChargePoints() == getMaxChargePoints()) {
            // Block the attack
            StatusMessageAnimationFactory statusFactory = injector.getInstance(StatusMessageAnimationFactory.class);
            addEffect(statusFactory.create(Color.GREEN, "BLOCKED!"));
            
            // Spend the charge points required to block the attack
            spendChargePoints((int) getMaxChargePoints());
        } 
        // If the shield bearer is not fully charged
        else {
            // Take damage like usual
            super.takeDamage(damage);
            
            // Accrue a charge point
            final int chargePointsGainedPerAttack = 1;
            gainChargePoints(chargePointsGainedPerAttack);
        }
    }
}
