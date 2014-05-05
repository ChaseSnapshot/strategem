package com.sneaky.stratagem.flow;

import java.util.Set;

import android.opengl.GLSurfaceView;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This update-render flow controller strives for a constant game speed with a maximum
 * rendered frames per second.
 * 
 * @author R. Matt McCann
 */
@Singleton
public class GameFlowControllerImpl extends GameFlowController {
    /** Maximum number of game updates that can occur before trying to render. */
    private final int maxSkippedRenders = 20;
    
    /** Tick period is milliseconds. */
    private final int tickPeriod;
    
    /** Desired game speed. */
    private final int ticksPerSecond = 50;
    
    /** Injection constructor. */
    @Inject
    public GameFlowControllerImpl(final GLSurfaceView renderer) {
        super(renderer);
        
        final int oneSecond = 1000;
        tickPeriod = oneSecond / ticksPerSecond;
    }
    
    /** {@inheritDoc} */
    @Override
    public void run() {
        int stateUpdatesSinceLastRender = 0;
        
        // Set the game loop as running
        setIsRunning(true);
        
        while (isRunning()) {
            final long flowStart = System.currentTimeMillis();
            long timeLeftInFlow;
            
            // Render a frame
            getRenderer().requestRender();
            stateUpdatesSinceLastRender = 0;
            
            // Keep updating the game state until we have enough time left to perform a rendering
            do {
                synchronized (this) {
                    Set<Updatable> updatables = getUpdatables();
                
                    // Add any updatable objects that requested themselves to be added
                    Set<Updatable> toBeAdded = getToBeAdded();
                    for (Updatable addMe : toBeAdded) {
                        updatables.add(addMe);
                    }
                    toBeAdded.clear();
                
                    // Remove any updatable objects that requested themselves for removal
                    Set<Updatable> toBeRemoved = getToBeRemoved();
                    for (Updatable removeMe : toBeRemoved) {
                        updatables.remove(removeMe);
                    }
                    toBeRemoved.clear();
                
                    // Update the game state
                    for (Updatable toBeUpdated : updatables) {
                        toBeUpdated.updateState(ticksPerSecond);
                    }
                }
                
                // Update the number up state updates that have occurred
                stateUpdatesSinceLastRender++;
                
                // Calculate how much time is left over after updating the state
                final long flowStopDeadline = flowStart + tickPeriod * stateUpdatesSinceLastRender;
                timeLeftInFlow = flowStopDeadline - System.currentTimeMillis();
            } while ((timeLeftInFlow < 0) && 
                     (stateUpdatesSinceLastRender < maxSkippedRenders));
            
            // Sleep until its time to render another frame
            if (timeLeftInFlow > 0) {
                try {
                    Thread.sleep(timeLeftInFlow);
                } catch (InterruptedException ex) { }
            }
        }
    }
}
