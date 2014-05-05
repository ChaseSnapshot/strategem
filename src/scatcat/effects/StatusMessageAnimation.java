package scatcat.effects;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.sneaky.stratagem.flow.GameFlowController;
import com.sneaky.stratagem.graphics.textures.TextureFactory;

import android.graphics.Paint.Align;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import scatcat.general.Constants;
import scatcat.graphics.Color;
import scatcat.graphics.MVP;
import scatcat.graphics.shaders.SimpleTexturedShader;

public class StatusMessageAnimation implements Effect {
    /** Damage Icon Settings. */
    private final float[] statusMessageAspectRatio = new float[1];
    private final float   statusMessageHeight = 0.3f;
    private int           statusMessageTexture;
    
    /** How far the damage icon has dropped so far. */
    private float distanceDropped = 0.0f;
    
    private final Injector injector;
    
    /** Whether or not the animation has been cleaned up. */
    private boolean isCleanedUp = false;
    
    /** How far the damage icon drops before the animation is finished. */
    private final float maxDistanceDropped = 0.6f;
    
    /** Percentage of total distance dropped per second. */
    private final float percentDropPerSecond = 1.0f;

    /** Whether or not the texture has finished being generated. */
    private boolean readyToRender = false;
    
    private final SimpleTexturedShader shader;
    
    /** Starting position of the damage icon. */
    private final float startingPosition = 1.1f;
    
    @Inject
    protected StatusMessageAnimation(
            @Assisted final Color color,
            @Assisted final String statusMessage,
            final GameFlowController flowController,
            final Injector injector,
            final SimpleTexturedShader shader,
            final TextureFactory textureFactory,
            final GLSurfaceView view) {
        this.injector = injector;
        this.shader = shader;
        
        view.queueEvent(new Runnable() {
            public void run() {
                statusMessageTexture = textureFactory.texturizeText(statusMessage, 
                        color, Align.CENTER, 60.0f, statusMessageAspectRatio);
                readyToRender = true;
            }
        });
        
        flowController.addUpdatable(this);
    }
    public interface StatusMessageAnimationFactory {
        StatusMessageAnimation create(Color color, String statusMessage);
    }
    
    @Override
    public void cleanUp() {
        final int numTextures = 1;
        GLES20.glDeleteTextures(numTextures, new int[]{statusMessageTexture}, Constants.NO_OFFSET);
        
        isCleanedUp = true;
    }

    @Override
    public StatusMessageAnimation clone() {
        return injector.getInstance(StatusMessageAnimation.class);
    }
    
    @Override
    public void render(MVP mvp) {
        if (!isCleanedUp && readyToRender) {
            // Move the damage icon to it current position and scale it size
            final float[] model = mvp.peekCopy(MVP.Type.MODEL);
            Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, startingPosition - distanceDropped, 0.01f);
            Matrix.scaleM(model, Constants.NO_OFFSET, statusMessageHeight * statusMessageAspectRatio[0], 
                    statusMessageHeight, 1.0f);
            
            // Render the icon
            shader.activate();
            shader.setMVPMatrix(mvp.collapseM(model));
            shader.setTexture(statusMessageTexture);
            shader.draw();
        }
    }

    @Override
    public void updateState(int updatesPerSecond) {
        distanceDropped += percentDropPerSecond / updatesPerSecond * maxDistanceDropped;
        
        if (distanceDropped >= maxDistanceDropped) {
            cleanUp();
        }
    }
}
