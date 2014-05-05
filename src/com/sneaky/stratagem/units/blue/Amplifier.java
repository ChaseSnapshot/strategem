package com.sneaky.stratagem.units.blue;

import java.util.ArrayList;
import java.util.List;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.points.Point3D;
import scatcat.graphics.MVP;
import scatcat.graphics.shaders.SimpleTexturedShader;
import android.opengl.Matrix;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.AttackAction.AttackActionFactory;
import com.sneaky.stratagem.actions.Executable;
import com.sneaky.stratagem.actions.MoveAction.MoveActionFactory;
import com.sneaky.stratagem.units.Unit;

public class Amplifier extends Unit {
    private final Injector injector;
    
    @Inject
    protected Amplifier(final Injector injector,
                        @Named("AmplifierFront") final int amplifierFront,
                        @Named("AmplifierBack") final int amplifierBack) {
        super(injector, amplifierFront, amplifierBack, amplifierFront, amplifierBack, amplifierFront);
        
        this.injector = injector;
        
        setName("Amplifier");
        setChargePoints(0);
        setApCostOfAttack(1);
        setApCostOfMovement(1);
        setAttackDamage(1);
        setAttackRange(1);
        setEnlistCost(2);
        setHealth(12);
        setMaxChargePoints(0);
        setMaxHealth(12);
        setMovementRange(2);
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
