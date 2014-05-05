package com.sneaky.stratagem.flow;

import java.util.HashSet;
import java.util.Set;

import android.opengl.GLSurfaceView;

/**
 * Controls the update-render flow of the game.
 * 
 * @author R. Matt McCann
 */
public abstract class GameFlowController extends Thread {
    /** Whether or not the game flow is running. */
    private boolean isRunning = false;
    
    /** Interface for triggering a render. */
    private final GLSurfaceView renderer;
    
    private final Set<Updatable> toBeAdded = new HashSet<Updatable>();
    
    private final Set<Updatable> toBeRemoved = new HashSet<Updatable>();
    
    /** All objects who require the opportunity to update their state during the update phase. */
    private final Set<Updatable> updatables = new HashSet<Updatable>();
    
    /** Injection renderer. */
    protected GameFlowController(final GLSurfaceView renderer) {
        this.renderer = renderer;
    }
    
    public synchronized void addUpdatable(final Updatable updatable) { 
        toBeAdded.add(updatable); 
    }
    
    protected final GLSurfaceView getRenderer() { return renderer; }
    protected final Set<Updatable> getToBeAdded() { return toBeAdded; }
    protected final Set<Updatable> getToBeRemoved() { return toBeRemoved; }
    protected final Set<Updatable> getUpdatables() { return updatables; }
    
    public final boolean isRunning() { return isRunning; }
    
    public synchronized void removeUpdatable(final Updatable updatable) {
        toBeRemoved.add(updatable); 
    }
    
    /** Starts the game. Controls all state updating and rendering flow. */
    @Override
    public abstract void run();
    
    public final void setIsRunning(boolean isRunning) { this.isRunning = isRunning; }
}
