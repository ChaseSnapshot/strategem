package com.sneaky.stratagem.units.commanders;

import java.util.ArrayList;
import java.util.List;

import scatcat.general.points.Point3D;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.AttackAction.AttackActionFactory;
import com.sneaky.stratagem.actions.Executable;
import com.sneaky.stratagem.actions.ManaBatteryAction.ManaBatteryActionFactory;
import com.sneaky.stratagem.actions.MoveAction.MoveActionFactory;
import com.sneaky.stratagem.actions.StunAction.StunActionFactory;
import com.sneaky.stratagem.actions.TeleportAction.TeleportActionFactory;
import com.sneaky.stratagem.units.commanders.MeteorStrike.MeteorStrikeFactory;

/** 
 * The Blue Wizard commander's class.
 * 
 * @author R. Matt McCann
 */
public class BlueWizard extends Commander {
    @Inject
    protected BlueWizard(final Injector injector, 
                         @Named("BlueWizardProfile") final int profileTexture,
                         @Named("BlueWizardNorth") final int facingNorthTexture,
                         @Named("BlueWizardEast") final int facingEastTexture,
                         @Named("BlueWizardSouth") final int facingSouthTexture,
                         @Named("BlueWizardWest") final int facingWestTexture) {
        super(injector, profileTexture, facingNorthTexture, facingEastTexture,
                facingSouthTexture, facingWestTexture);

        setName("Blue Wizard");
        setChargePoints(0);
        setApCostOfAttack(1);
        setApCostOfMovement(1);
        setAttackDamage(5);
        setAttackRange(4);
        setHealth(20);
        setMaxChargePoints(5);
        setMaxHealth(20);
        setMovementRange(2);
        setNumPassivelyAccruedChargePoints(1);
        setTextureOffset(new Point3D(0.0f, 0.50f, 0.05f));
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
        
        // Add the stun action
        StunActionFactory stunActionFactory = injector.getInstance(StunActionFactory.class);
        if (stunActionFactory != null) {
            actions.add(stunActionFactory.create(this));
        }
        
        // Add the mana battery action
        ManaBatteryActionFactory manaBatteryFactory = injector.getInstance(ManaBatteryActionFactory.class);
        if (manaBatteryFactory != null) {
            actions.add(manaBatteryFactory.create(this));
        }
        
        // Add the teleport battery action
        TeleportActionFactory teleportFactory = injector.getInstance(TeleportActionFactory.class);
        if (teleportFactory != null) {
            actions.add(teleportFactory.create(this));
        }
        
        // Add the meteor strike action
        MeteorStrikeFactory meteorStrikeFactory = injector.getInstance(MeteorStrikeFactory.class);
        if (meteorStrikeFactory != null) {
            actions.add(meteorStrikeFactory.create(this));
        }
        
        setActions(actions);
    }
}
