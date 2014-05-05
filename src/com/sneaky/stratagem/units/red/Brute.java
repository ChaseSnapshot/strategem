package com.sneaky.stratagem.units.red;

import java.util.ArrayList;
import java.util.List;

import scatcat.general.points.Point3D;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.AttackAction.AttackActionFactory;
import com.sneaky.stratagem.actions.BashAction.BashActionFactory;
import com.sneaky.stratagem.actions.Executable;
import com.sneaky.stratagem.actions.MoveAction.MoveActionFactory;
import com.sneaky.stratagem.units.Unit;

public class Brute extends Unit {
    @Inject
    protected Brute(final Injector injector,
                    @Named("BruteFront") final int bruteFront) {
        super(injector, bruteFront, bruteFront, bruteFront, bruteFront, bruteFront);
        
        setName("Brute");
        setChargePoints(0);
        setApCostOfAttack(1);
        setApCostOfMovement(1);
        setAttackDamage(5);
        setAttackRange(1);
        setEnlistCost(2);
        setHealth(15);
        setMaxChargePoints(5);
        setMaxHealth(15);
        setMovementRange(1);
        setNumPassivelyAccruedChargePoints(1);
        setTextureOffset(new Point3D(-0.02f, 0.38f, 0.075f));
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
        
        // Add the bash action
        BashActionFactory bashFactory = injector.getInstance(BashActionFactory.class);
        if (bashFactory != null) {
            actions.add(bashFactory.create(this));
        }
        
        setActions(actions);
    }
}
