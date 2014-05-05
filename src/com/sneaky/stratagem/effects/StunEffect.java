package com.sneaky.stratagem.effects;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sneaky.stratagem.flow.EndRoundEvent;
import com.sneaky.stratagem.units.Unit;

public class StunEffect {
    private int roundsRemaining;
    
    private final Unit target;
    
    @Inject
    protected StunEffect(@Assisted int duration,
                         @Assisted Unit target,
                         EventBus notifier) {
        this.roundsRemaining = duration;
        this.target = target;
    
        target.setIsStunned(true);
        notifier.register(this);
    }
    
    public interface StunEffectFactory {
        StunEffect create(int duration, Unit target);
    }
    
    @Subscribe
    public void listenForEndOfRound(final EndRoundEvent e) {
        roundsRemaining--;
        
        // If the stun effect has ended
        if (roundsRemaining == 0) {
            target.setIsStunned(false);
            target.removeEffect(this);
        }
    }
}
