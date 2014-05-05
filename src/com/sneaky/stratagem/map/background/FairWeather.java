package com.sneaky.stratagem.map.background;

import java.util.PriorityQueue;
import java.util.Random;

import scatcat.graphics.Color;
import scatcat.graphics.MVP;
import android.opengl.GLES20;

import com.google.inject.Inject;
import com.sneaky.stratagem.flow.GameFlowController;

public class FairWeather extends Background {
    /** Clouds that float around in the background. */
    private PriorityQueue<Cloud> clouds;
    
    @Inject
    public FairWeather(final CloudFactory cloudFactory,
                      final GameFlowController flowController) {
        // Generate a random number of clouds
        final Random randomizer = new Random();
        final int minClouds = 10;
        final int numCloudsRange = 10;
        final int numClouds = minClouds + randomizer.nextInt(numCloudsRange);
        clouds = new PriorityQueue<Cloud>(numClouds, new Cloud.CloudComparer());
        for (int cloudIter = 0; cloudIter < numClouds; cloudIter++) {
            Cloud cloud = cloudFactory.createRandomCloud();
            
            flowController.addUpdatable(cloud);
            clouds.add(cloud);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void render(MVP mvp) {
        // Set the background color
        final Color background = Color.SKY_BLUE;
        GLES20.glClearColor(background.getRed(), background.getGreen(), 
                            background.getBlue(), background.getAlpha());
        
        // Render the clouds
        PriorityQueue<Cloud> newClouds = new PriorityQueue<Cloud>(clouds.size(), clouds.comparator());
        while (!clouds.isEmpty()) {
            Cloud cloud = clouds.remove();
            cloud.render(mvp);
            newClouds.add(cloud);
        }
        clouds = newClouds;
    }
}
