package com.sneaky.stratagem.match;

//import java.util.ArrayList;
//import java.util.Arrays;

import com.sneaky.stratagem.R;
import com.sneaky.stratagem.graphics.textures.TextureFactory;

import scatcat.general.Device;
import scatcat.graphics.shaders.SimpleTexturedShader;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * A local match between two players.
 * 
 * @author R. Matt McCann
 */
@Singleton
public class LocalMatch extends Match {
    
    @Inject
    protected LocalMatch(final Device device,
                         final Injector injector,
                         final SimpleTexturedShader shader,
                         final TextureFactory textureFactory) {
        super(device, injector, shader, textureFactory);
        
        // Configure the players
        Player playerOne = new Player(device, shader, textureFactory);
        playerOne.setCurrentActionPoints(10);
        playerOne.setMaxActionPoints(10);
        playerOne.setName("Moxximus");
        //playerOne.setUnitColorTexture(textureFactory.loadTexture(R.drawable.overlays_team_red));
        
        Player playerTwo = new Player(device, shader, textureFactory);
        playerTwo.setCurrentActionPoints(10);
        playerTwo.setMaxActionPoints(10);
        playerTwo.setName("Hullabaloo");
        //playerTwo.setUnitColorTexture(textureFactory.loadTexture(R.drawable.overlays_team_blue));
        
        //setPlayers(new ArrayList<Player>(Arrays.asList(playerOne, playerTwo)));
    }
}
