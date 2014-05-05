package com.sneaky.stratagem.match;

import java.util.ArrayList;
import java.util.List;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.Dimension;
import scatcat.graphics.Color;
import scatcat.graphics.MVP;
import scatcat.graphics.shaders.SimpleTexturedShader;
import android.graphics.Paint.Align;
import android.opengl.Matrix;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.sneaky.stratagem.flow.EndRoundEvent;
import com.sneaky.stratagem.flow.EndTurnEvent;
import com.sneaky.stratagem.graphics.textures.TextureFactory;

/**
 * Model representing a match.
 * 
 * @author R. Matt McCann
 */
@Singleton
public class Match {
    /** Armies in the match. */
    private final List<Army> armies = new ArrayList<Army>();
    
    /** Player whose turn it currently is. */
    private int currentPlayer = 0;
    
    private final Device device;
    
    private final Injector injector;
    
    /** Players in the match order according to play position. */
    private final List<Player> players = new ArrayList<Player>();
    
    /** Interface for rendering. */
    private final SimpleTexturedShader shader;
    
    /** Current turn. */
    private int round = 1;
    
    /** Texture of the current turn. */
    private int turnTexture;
    
    /** Dimensions of the turn texture. */
    private Dimension turnTextureSize = new Dimension();
    
    @Inject
    protected Match(final Device device,
                    final Injector injector,
                    final SimpleTexturedShader shader,
                    final TextureFactory textureFactory) {
        this.device = device;
        this.injector = injector;
        this.shader = shader;
        
        final float fontSize = 20.0f;
        turnTexture = textureFactory.texturizeText("Round 1", Color.WHITE, Align.RIGHT, fontSize, turnTextureSize);
    }
    
    public final void addPlayer(final Player player, final Army army) { 
        armies.add(checkNotNull(army));
        players.add(checkNotNull(player));
    }
    
    /** Finishes the current player's turn. */
    public final void finishTurn() {
        Player player = players.get(currentPlayer);
        
        // Signal to all interested objects that this player's turn has ended
        EventBus notifier = injector.getInstance(EventBus.class);
        notifier.post(new EndTurnEvent(player));
        
        // Reset the current player's AP count
        player.setCurrentActionPoints(player.getMaxActionPoints());
        
        // Move to the next player's turn
        currentPlayer = (currentPlayer + 1) % players.size();
        
        // If each player has played during the round
        if (currentPlayer == 0) {
            // Signal to all interested objects that this round has ended
            notifier.post(new EndRoundEvent());
            
            // Move to the next round
            round++;
        }
    }
    
    public final int getCurrentActionPoints() { return players.get(currentPlayer).getCurrentActionPoints(); }
    public final Army getCurrentArmy() { return armies.get(currentPlayer); }
    public final Player getCurrentPlayer() { return players.get(currentPlayer); }
    public final int getNumPlayers() { return players.size(); }
    public final int getRound() { return round; }
    public final Army getArmy(int pos) { return armies.get(pos); }
    public final Player getPlayer(int pos) { return players.get(pos); }
    
    /**
     * Renders the current round.
     * 
     * @param mvp Model-View-Projection matrices.
     */
    public final void renderCurrentRound(final MVP mvp) {
        // Move the texture to correct place
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        final float translateX = (device.getWidth() - turnTextureSize.getWidth()) / 2.0f;
        final float translateY = -1.5f * turnTextureSize.getHeight();
        Matrix.translateM(model, Constants.NO_OFFSET, translateX, translateY, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, turnTextureSize.getWidth(), turnTextureSize.getHeight(), 1.0f);
        
        // Render the texture
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(turnTexture);
        shader.draw();
    }
    
    public final void spendActionPoints(final int spentPoints) {
        // Update the player's action points
        Player player = players.get(currentPlayer);
        int actionPoints = player.getCurrentActionPoints();
        actionPoints -= spentPoints;
        
        // If the player is out of action points, move to the next player
        if (actionPoints == 0) {
            player.setCurrentActionPoints(player.getMaxActionPoints());
            finishTurn();
        } else {
            player.setCurrentActionPoints(actionPoints);
        }
    }
}
