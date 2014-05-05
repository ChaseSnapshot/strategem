package com.sneaky.stratagem.units.red;

import java.util.ArrayList;
import java.util.List;

import scatcat.general.points.Point3D;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.Executable;
import com.sneaky.stratagem.actions.MoveAction.MoveActionFactory;
import com.sneaky.stratagem.units.Unit;

public class Drummer extends Unit {
    @Inject
    protected Drummer(final Injector injector,
                      @Named("DrummerFront") final int drummerFront) {
        super(injector, drummerFront, drummerFront, drummerFront, drummerFront, drummerFront);

        setName("Drummer");
        setChargePoints(0);
        setApCostOfAttack(1);
        setApCostOfMovement(1);
        setEnlistCost(1);
        setHealth(10);
        setMaxChargePoints(0);
        setMaxHealth(10);
        setMovementRange(3);
        setTextureOffset(new Point3D(0.0f, 0.5f, 0.0f));
        
        List<Executable> actions = new ArrayList<Executable>();
        
        // Add the move action
        MoveActionFactory moveFactory = injector.getInstance(MoveActionFactory.class);
        if (moveFactory != null) {
            actions.add(moveFactory.create(this));
        }
        
        setActions(actions);
    }
}
