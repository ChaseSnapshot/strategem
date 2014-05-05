package com.sneaky.stratagem.match;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.sneaky.stratagem.graphics.textures.TextureFactory;

import android.graphics.Paint;
import android.opengl.Matrix;
import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.graphics.Color;
import scatcat.graphics.MVP;
import scatcat.graphics.shaders.SimpleTexturedShader;

public class Player {
    /** Current action points available. */
    private int currentActionPoints;
    
    private final Device device;
    
    /** Total action points available per round. */
    private int maxActionPoints;
    
    /** This player's turn texture. */
    private final float       myTurnHeight = 0.05f;
    private Optional<Integer> myTurnTexture = Optional.<Integer>absent();
    private float             myTurnWidth;
    
    
    /** Name of the player. */
    private String name;
    
    /** Shader used for rendering the HUD. */
    private final SimpleTexturedShader shader;
    
    /** Used for generating the "My Turn" text. */
    private final TextureFactory textureFactory;
    
    /** Color of the player's team. */
    private Color teamColor;
    
    /** Tile overlay sporting the player's color. */
    private int teamColorTexture;
    
    @Inject
    public Player(final Device device,
                  final SimpleTexturedShader shader,
                  final TextureFactory textureFactory) {
        this.device = device;
        this.shader = shader;
        this.textureFactory = textureFactory;
    }
    
    /**
     * Renders "<Player's Name>'s Turn" in the HUD.
     * 
     * @param mvp Model-View-Projection matrices.
     */
    public final void renderMyTurn(final MVP mvp) {
        // Generate the texture if it hasn't already been generated
        if (!myTurnTexture.isPresent()) {
            final float[] aspectRatio = new float[1];
            myTurnTexture = Optional.<Integer>of(textureFactory.texturizeText(name + "'s Turn", 
                    Color.WHITE, Paint.Align.LEFT, 60.0f, aspectRatio));
            myTurnWidth = myTurnHeight * aspectRatio[0] / device.getAspectRatio();
        }
        
        // Move the texture to the correct place
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        final float translateX = 0.0f; //-0.5f + myTurnWidth / 2.0f;
        final float translateY = -myTurnHeight / 2.0f;
        Matrix.translateM(model, Constants.NO_OFFSET, translateX, translateY, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, myTurnWidth, myTurnHeight, 1.0f);
        
        // Render the texture
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(myTurnTexture.get());
        shader.draw();
    }
    
    public final int getCurrentActionPoints() { return currentActionPoints; }
    public final int getMaxActionPoints() { return maxActionPoints; }
    public final String getName() { return name; }
    public final Color getTeamColor() { return teamColor; }
    public final int getTeamColorTexture() { return teamColorTexture; }
    
    public final void setCurrentActionPoints(final int currentActionPoints) { 
        this.currentActionPoints = currentActionPoints; }
    public final void setMaxActionPoints(final int maxActionPoints) {
        this.maxActionPoints = maxActionPoints; }
    public final void setName(final String name) { this.name = name; }
    public final void setTeamColor(final Color teamColor) { this.teamColor = teamColor; }
    public final void setTeamColorTexture(final int teamColorTexture) { this.teamColorTexture = teamColorTexture; }
}
