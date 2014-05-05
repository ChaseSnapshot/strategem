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
import com.sneaky.stratagem.units.Unit;

public class Armor extends Unit {
    @Inject
    protected Armor(final Injector injector,
                    @Named("ArmorFront") final int armorFront,
                    @Named("ArmorBack") final int armorBack) {
        super(injector, armorFront, armorBack, armorFront, armorBack, armorFront);
        
        setName("Armor");
        setChargePoints(0);
        setApCostOfAttack(1);
        setApCostOfMovement(1);
        setAttackDamage(5);
        setAttackRange(1);
        setEnlistCost(1);
        setHealth(20);
        setMaxChargePoints(0);
        setMaxHealth(20);
        setMovementRange(1);
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
}
