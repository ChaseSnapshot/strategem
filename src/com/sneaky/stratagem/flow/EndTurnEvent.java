package com.sneaky.stratagem.flow;

import com.sneaky.stratagem.match.Player;

/**
 * Event trigger used to notify interested objects that a player's turn has ended.
 * 
 * @author R. Matt McCann
 */
public class EndTurnEvent {
    /** The player who ended their turn. */
    private final Player player;
    
    public EndTurnEvent(final Player player) {
        this.player = player;
    }
    
    public Player getPlayer() { return player; }
}
