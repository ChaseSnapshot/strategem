package com.sneaky.stratagem.units.commanders;

import java.util.ArrayList;
import java.util.List;

import scatcat.general.points.Point3D;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.AttackAction.AttackActionFactory;
import com.sneaky.stratagem.actions.ChargeAction.ChargeActionFactory;
import com.sneaky.stratagem.actions.EarthSplitterAction.EarthSplitterActionFactory;
import com.sneaky.stratagem.actions.Executable;
import com.sneaky.stratagem.actions.MoveAction.MoveActionFactory;
import com.sneaky.stratagem.actions.WarCry.WarCryFactory;
public class RedKnight extends Commander {
    @Inject
    protected RedKnight(final Injector injector,
                        @Named("RedKnightProfile") final int profileTexture,
                        @Named("RedKnightNorth") final int facingNorthTexture,
                        @Named("RedKnightEast") final int facingEastTexture,
                        @Named("RedKnightSouth") final int facingSouthTexture,
                        @Named("RedKnightWest") final int facingWestTexture) {
        super(injector, profileTexture, facingNorthTexture, facingEastTexture,
                facingSouthTexture, facingWestTexture);
        
        setName("Red Knight");
        setChargePoints(0);
        setApCostOfAttack(1);
        setApCostOfMovement(1);
        setAttackDamage(6);
        setAttackRange(1);
        setHealth(30);
        setMaxChargePoints(10);
        setMaxHealth(30);
        setMovementRange(3);
        setNumPassivelyAccruedChargePoints(1);
        setTextureOffset(new Point3D(0.0f, 0.45f, 0.05f));
        setTextureScale(1.25f);
        
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
        
        // Add the charge action
        ChargeActionFactory chargeFactory = injector.getInstance(ChargeActionFactory.class);
        if (chargeFactory != null) {
            actions.add(chargeFactory.create(this));
        }
        
        // Add the war cry action
        WarCryFactory warCryFactory = injector.getInstance(WarCryFactory.class);
        if (warCryFactory != null) {
            actions.add(warCryFactory.create(this));
        }
        
        // Add the earth splitter action
        EarthSplitterActionFactory earthSplitter = injector.getInstance(EarthSplitterActionFactory.class);
        if (earthSplitter != null) {
            actions.add(earthSplitter.create(this));
        }
        
        setActions(actions);
    }
}
