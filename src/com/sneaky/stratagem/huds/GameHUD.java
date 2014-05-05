package com.sneaky.stratagem.huds;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.Matrix;
import com.sneaky.stratagem.R;
import com.sneaky.stratagem.graphics.textures.TextureFactory;
import com.sneaky.stratagem.graphics.widgets.Button;
import com.sneaky.stratagem.graphics.widgets.Button.ButtonFactory;
import com.sneaky.stratagem.match.Match;
import com.sneaky.stratagem.match.Player;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.Dimension;
import scatcat.general.points.NormalizedPoint2D;
import scatcat.general.points.Point2D;
import scatcat.graphics.Color;
import scatcat.graphics.MVP;
import scatcat.graphics.shaders.SimpleTexturedShader;
import scatcat.input.InputHelper;

@Singleton
public class GameHUD extends HUD {
    /** Aspect ratio of the action points texture. */
    private final float[] actionPointsAspectRatio = new float[1];
    
    /** Texture used to display the action points. */
    private int actionPointsTexture = -1;
    
    /** Background texture for the action point counter. */
    private Optional<Integer> apBackgroundTexture = Optional.<Integer>absent();
    
    /** Used for retrieving the screens aspect ratio. */
    private final Device device;
    
    /** "Finish Turn" Button. */
    private final Button finishTurnButton;
    private final float finishTurnHeight = 0.1f;
    private final float finishTurnWidth = 0.12f;
    private final float finishTurnXPos;
    private final float finishTurnYPos = 0.425f;
    private final float finishTurnTextHeightRatio = 0.7f;
    
    /** Current frames per second. */
    private float fps = -1.0f;
    
    /** Rendering size of FPS. */
    private Dimension fpsSize = new Dimension();
    
    /** Texture handle of the FPS. */
    private int fpsTexture = -1;
    
    private final Injector injector;
    
    /** Settings and values of the match. */
    private final Match match;
    
    /** Timestamp of the last frame rendering. */
    private long lastFrameTime;
    
    /** Action point value used in the current action points texture. */
    private int renderedActionPoints = -1;
    
    /** Shader used for rendering the HUD. */
    private final SimpleTexturedShader shader;
    
    /** Used for generating textures. */
    private final TextureFactory textureFactory;
    
    /** Injection constructor. */
    @Inject
    protected GameHUD(@Named("SimpleButtonTexture") final int buttonBackgroundTexture,
                      final ButtonFactory buttonFactory,
                      final Device device,
                      final Injector injector,
                      final Match match,
                      final SimpleTexturedShader shader,
                      final TextureFactory textureFactory) {
        this.device = device;
        this.injector = injector;
        this.match = match;
        this.shader = shader;
        this.textureFactory = textureFactory;
        
        this.finishTurnButton = buttonFactory.create(buttonBackgroundTexture, finishTurnHeight, 
                "Finish Turn", finishTurnTextHeightRatio, finishTurnWidth);
        finishTurnXPos = -0.5f + 0.025f / device.getAspectRatio() + finishTurnWidth / 2.0f;
    }
    
    @Override
    public boolean handleClick(NormalizedPoint2D clickLocation) {
        // Check if the "Finish Turn" button is clicked
        Point2D position = new Point2D(finishTurnXPos, finishTurnYPos);
        if (InputHelper.isTouched(position, finishTurnWidth, finishTurnHeight, clickLocation)) {
            Match match = injector.getInstance(Match.class);
            match.finishTurn();
            
            return true;
        }

        return false;
    }

    @Override
    public final void render() {
        // Disable depth testing for the flat "on-screen" HUD
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        
        shader.activate();
        
        MVP mvp = new MVP();
        mvp.push(MVP.Type.PROJECTION, getProjection());
        
        // Render the "Finish Turn" button
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, finishTurnXPos, finishTurnYPos, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        finishTurnButton.render(mvp);
        mvp.pop(MVP.Type.MODEL);
        
        // Move to the top of the screen
        model = mvp.peekCopy(MVP.Type.MODEL);
        final float topOfScreen = 0.5f;
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, topOfScreen, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        
        renderApBackground(mvp);
        renderActionPoints(mvp);
        renderFPS(mvp);
        match.getCurrentPlayer().renderMyTurn(mvp);
        //match.renderCurrentRound(mvp);
        
        mvp.pop(MVP.Type.MODEL);
        
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }
    
    /**
     * Renders the action points available to the current player.
     * 
     * @param mvp Model-View-Projection matrices.
     */
    private void renderActionPoints(final MVP mvp) {
        Player player = match.getCurrentPlayer();
        
        final float actionPointsHeight = 0.09f;
        
        // If the action points texture needs to be updated
        if (renderedActionPoints != player.getCurrentActionPoints()) {
            // Release the previous action points texture
            if (actionPointsTexture != -1) {
                GLES20.glDeleteTextures(1, new int[]{actionPointsTexture}, Constants.NO_STRIDE);
            }
            
            // Generate the new action points texture
            final float actionPointsFontSize = 60.0f;
            actionPointsTexture = textureFactory.texturizeText(
                    Integer.toString(player.getCurrentActionPoints()), Color.WHITE, Paint.Align.CENTER,
                    actionPointsFontSize, actionPointsAspectRatio);
        }
        
        // Position the texture
        //final float xTranslate = (Device.getWidth() * 0.95f - actionPointsSize.getWidth()) / 2.0f;
        final float xTranslate = 0.45f;
        final float yTranslate = -actionPointsHeight / 2.0f - 0.04f;
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, xTranslate, yTranslate, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, actionPointsHeight * actionPointsAspectRatio[0] / device.getAspectRatio(), actionPointsHeight, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        
        // Draw the texture
        shader.setTexture(actionPointsTexture);
        shader.draw();
    }
    
    /** Renders the background of the AP counter. */
    private final void renderApBackground(final MVP mvp) {
        // If the background texture has not yet been loaded, do so
        if (!apBackgroundTexture.isPresent()) {
            apBackgroundTexture = Optional.<Integer>of(textureFactory.loadTexture(R.drawable.gui_ap_counter));
        }
        
        // Position the texture
        final float height = 0.3f;
        final float width = height / device.getAspectRatio();
        final float xTranslate = (1.0f - width) / 2.0f;
        final float yTranslate = -height / 2.0f;
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, xTranslate, yTranslate, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, width, height, 1.0f);
        
        // Draw the texture
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(apBackgroundTexture.get());
        shader.draw();
    }
    
    /**
     * Renders the frames per second.
     * 
     * @param mvp Model-View-Projection matrices.
     */
    private void renderFPS(final MVP mvp) {
        // Calculate the running average FPS
        float newFPS;
        if (fps >= 0) {
            newFPS = (float) (fps * 0.9 + (1000 / (System.currentTimeMillis() - lastFrameTime)) * 0.1);
        } else {
            newFPS = 1000.0f / (System.currentTimeMillis() - lastFrameTime);
        }
        lastFrameTime = System.currentTimeMillis();
        
        // Update the FPS texture if necessary
        if (Math.floor(fps) != Math.floor(newFPS)) {
            // Release the previous FPS texture
            int[] deleteFPSTexture = new int[] {fpsTexture};
            if (fpsTexture != -1) {
                GLES20.glDeleteTextures(1, deleteFPSTexture, Constants.NO_STRIDE);
            }
            
            // Generate the new FPS texture
            final int fpsFontSize = 20;
            fpsTexture = textureFactory.texturizeText(Integer.toString((int) Math.floor(newFPS)), 
                    Color.WHITE, Paint.Align.LEFT, fpsFontSize, fpsSize);
        }
        fps = newFPS;
        
        // Set the MVP Matrix
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        float yTranslate = -fpsSize.getHeight() / 2.0f;
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, yTranslate, 0.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, fpsSize.getWidth(), fpsSize.getHeight(), 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        
        // Draw the texture
        shader.setTexture(fpsTexture);
        shader.draw();
    }

    @Override
    public boolean handleLongPress(NormalizedPoint2D pressLocation) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean handlePickUp(NormalizedPoint2D touchLocation) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean handleDrag(NormalizedPoint2D moveVector) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean handleDrop(NormalizedPoint2D dropLocation) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean handleZoom(float zoomFactor) {
        // TODO Auto-generated method stub
        return false;
    }
}
