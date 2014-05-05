package com.sneaky.stratagem.units.blue;

import java.util.ArrayList;
import java.util.List;

import scatcat.general.points.Point3D;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.AttackAction.AttackActionFactory;
import com.sneaky.stratagem.actions.Executable;
import com.sneaky.stratagem.actions.MoveAction.MoveActionFactory;
import com.sneaky.stratagem.actions.RaiseHand.RaiseHandFactory;
import com.sneaky.stratagem.units.Unit;

public class HandMage extends Unit {
    @Inject
    protected HandMage(final Injector injector,
                       @Named("HandMageFront") final int handMageFront,
                       @Named("HandMageBack") final int handMageBack) {
        super(injector, handMageFront, handMageBack, handMageFront, handMageBack, handMageFront);

        setName("Hand Mage");
        setChargePoints(0);
        setApCostOfAttack(1);
        setApCostOfMovement(1);
        setAttackDamage(3);
        setAttackRange(3);
        setEnlistCost(2);
        setHealth(15);
        setMaxChargePoints(5);
        setMaxHealth(15);
        setMovementRange(2);
        setNumPassivelyAccruedChargePoints(1);
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
        
        // Add the summan hand action
        RaiseHandFactory raiseHandFactory = injector.getInstance(RaiseHandFactory.class);
        if (raiseHandFactory != null) {
            actions.add(raiseHandFactory.create(this));
        }
        
        setActions(actions);
    }
}