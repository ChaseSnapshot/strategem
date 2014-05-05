package com.sneaky.stratagem.units.red;

import java.util.ArrayList;
import java.util.List;

import scatcat.general.points.Point3D;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.AttackAction.AttackActionFactory;
import com.sneaky.stratagem.actions.Executable;
import com.sneaky.stratagem.actions.MoveAction.MoveActionFactory;
import com.sneaky.stratagem.units.Unit;

public class Engie extends Unit {
    @Inject
    protected Engie(final Injector injector,
                    @Named("EngieFront") final int engieFront) {
        super(injector, engieFront, engieFront, engieFront, engieFront, engieFront);

        setName("Anti-Magic Engine");
        setChargePoints(5);
        setApCostOfAttack(1);
        setApCostOfMovement(1);
        setAttackDamage(3);
        setAttackRange(2);
        setEnlistCost(3);
        setHealth(15);
        setMaxChargePoints(5);
        setMaxHealth(15);
        setMovementRange(1);
        setTextureOffset(new Point3D(0.0f, 0.45f, 0.05f));
        setTextureScale(1.5f);
        
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
}
