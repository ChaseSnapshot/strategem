package com.sneaky.stratagem.units.blue;

import java.util.ArrayList;
import java.util.List;

import scatcat.effects.StatusMessageAnimation.StatusMessageAnimationFactory;
import scatcat.general.points.Point3D;
import scatcat.graphics.Color;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.AttackAction.AttackActionFactory;
import com.sneaky.stratagem.actions.Executable;
import com.sneaky.stratagem.actions.MoveAction.MoveActionFactory;
import com.sneaky.stratagem.flow.EndTurnEvent;
import com.sneaky.stratagem.units.Unit;

public class Elemental extends Unit {
    private final Injector injector;
    
    @Inject
    protected Elemental(final Injector injector,
                        @Named("ElementalFront") final int elementalFront,
                        @Named("ElementalBack") final int elementalBack) {
        super(injector, elementalFront, elementalBack, elementalFront, elementalBack, elementalFront);
        
        this.injector = injector;
        
        setName("Elemental");
        setChargePoints(0);
        setApCostOfAttack(1);
        setApCostOfMovement(1);
        setAttackDamage(3);
        setAttackRange(1);
        setEnlistCost(2);
        setHealth(25);
        setMaxChargePoints(0);
        setMaxHealth(25);
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
    
    @Subscribe
    public void listenForEndOfTurn(EndTurnEvent event) {
        // If my owner just ended his/her turn and I haven't acted or moved
        if ((event.getPlayer() == getOwner()) && !hasMoved() && !hasActed()) {
            final int passivelyHealed = 1;
            
            // If the unit isn't at max health
            if (getHealth() != getMaxHealth()) {
                // Display an amount healed message
                StatusMessageAnimationFactory messageFactory = injector.getInstance(StatusMessageAnimationFactory.class);
                addEffect(messageFactory.create(Color.GREEN, "+" + passivelyHealed));
            }
            
            gainHealth(passivelyHealed);
        }
    }
}
