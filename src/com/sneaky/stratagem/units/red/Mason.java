package com.sneaky.stratagem.units.red;

import java.util.ArrayList;
import java.util.List;

import scatcat.general.points.Point3D;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.AttackAction.AttackActionFactory;
import com.sneaky.stratagem.actions.BuildWallAction.BuildWallActionFactory;
import com.sneaky.stratagem.actions.Executable;
import com.sneaky.stratagem.actions.MoveAction.MoveActionFactory;
import com.sneaky.stratagem.units.Unit;

public class Mason extends Unit {
    @Inject
    protected Mason(final Injector injector,
                    @Named("MasonBack") final int masonBack,
                    @Named("MasonFront") final int masonFront) {
        super(injector, masonFront, masonBack, masonFront, masonBack, masonFront);

        setName("Battle Mason");
        setChargePoints(1);
        setApCostOfAttack(1); 
        setApCostOfMovement(1);
        setAttackDamage(1);
        setAttackRange(1);
        setEnlistCost(1);
        setHealth(10);
        setMaxChargePoints(3);
        setMaxHealth(10);
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
        
        // Add the build wall action
        BuildWallActionFactory buildFactory = injector.getInstance(BuildWallActionFactory.class);
        if (buildFactory != null) {
            actions.add(buildFactory.create(this));
        }

        setActions(actions);
    }
}
