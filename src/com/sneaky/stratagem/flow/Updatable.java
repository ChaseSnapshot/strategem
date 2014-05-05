package com.sneaky.stratagem.flow;

/**
 * Provides a callback interface for game objects that need to update their state as
 * the game flow progresses.
 * 
 * @author R. Matt McCann
 */
public interface Updatable {
    /**
     * Updates the state of the object.
     */
    void updateState(int updatesPerSecond);
}
