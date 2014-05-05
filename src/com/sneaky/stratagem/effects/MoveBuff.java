package com.sneaky.stratagem.effects;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sneaky.stratagem.flow.EndRoundEvent;
import com.sneaky.stratagem.units.Unit;

public class MoveBuff {
    private final int buffAmount;
    
    private final EventBus notifier;
    
    private int roundsRemaining;
    
    private final Unit target;
    
    @Inject
    protected MoveBuff(@Assisted("buffAmount") final int buffAmount,
                       @Assisted("duration") final int duration,
                       @Assisted("target") final Unit target,
                       final EventBus notifier) {
        this.buffAmount = buffAmount;
        this.roundsRemaining = duration;
        this.notifier = notifier;
        this.target = target;
        
        // Apply the move buff as an active effect
        target.addEffect(this);
        
        // Increase the buffed units movement range
        final int currentMovementRange = target.getMovementRange();
        target.setMovementRange(currentMovementRange + buffAmount);
        
        // Start the round count down
        notifier.register(this);
    }
    
    public interface MoveBuffFactory {
        MoveBuff create(@Assisted("buffAmount") final int buffAmount,
                        @Assisted("duration") final int duration,
                        @Assisted("target") final Unit target);
    }
    
    @Subscribe
    public void listenForEndOfRound(final EndRoundEvent e) {
        roundsRemaining--;
        
        // If the buff effect has ended
        if (roundsRemaining == 0) {            
            // Remove the movement buff
            final int currentMovementRange = target.getMovementRange();
            target.setMovementRange(currentMovementRange - buffAmount);
            target.removeEffect(this);
            
            // Stop notifying this buff of rounds ending
            notifier.unregister(this);
        }
    }
}
