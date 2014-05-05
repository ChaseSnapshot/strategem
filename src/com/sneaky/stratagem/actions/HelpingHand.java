package com.sneaky.stratagem.actions;

import android.opengl.Matrix;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.sneaky.stratagem.flow.EndRoundEvent;
import com.sneaky.stratagem.flow.GameFlowController;
import com.sneaky.stratagem.flow.Updatable;
import com.sneaky.stratagem.match.Player;
import com.sneaky.stratagem.units.Unit;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.points.Point3D;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.shaders.SimpleTexturedShader;

public class HelpingHand implements RenderableMVP, Updatable {
    private final Player actingUnitOwner;
    
    private float currentYPos;
    
    private final Device device;
    
    private final Unit heldUnit;
    
    private final GameFlowController gameFlow;
    
    private final float maxYPos = -0.1f;

    private final Point3D heldUnitOriginalTextureOffet;
    
    private final EventBus notifier;
    
    private final float riseSpeed = 0.5f;
    
    private int roundsRemaining = 3;
    
    private final SimpleTexturedShader shader;
    
    private final float startingYPos = -0.7f;
    
    private State state = State.INIT;
    
    private final int texture;
    
    private enum State {
        INIT, RISING, HOLDING, FALLING, DONE
    }
    
    @Inject
    protected HelpingHand(@Assisted final Player actingUnitOwner,
                          final Device device,
                          final GameFlowController gameFlow,
                          @Assisted final Unit heldUnit,
                          final EventBus notifier,
                          final SimpleTexturedShader shader,
                          @Named("HelpingHandTexture") final int texture) {
        this.actingUnitOwner = actingUnitOwner;
        this.currentYPos = startingYPos;
        this.device = device;
        this.gameFlow = gameFlow;
        this.heldUnit = heldUnit;
        this.heldUnitOriginalTextureOffet = heldUnit.getTextureOffset();
        this.notifier = notifier;
        this.shader = shader;
        this.texture = texture;
    }
    
    public interface HelpingHandFactory {
        HelpingHand create(Player actingUnitOwner, Unit heldUnit);
    }
    
    private void done() {
        // Allow the unit to act and move again
        heldUnit.setCanAct(true);
        heldUnit.setCanMove(true);
        
        // Reset the held unit's texture offset (its possible during a slow-down for the texture
        // offset to be slightly modified)
        heldUnit.setTextureOffset(heldUnitOriginalTextureOffet);
        
        // Remove me as a rendering decoration from the held unit
        heldUnit.removeBackgroundDecoration(this);
        
        // Stop me from being updated
        gameFlow.removeUpdatable(this);
        notifier.unregister(this);
    }
    
    private void falling(final int updatesPerSecond) {
        // If the hand has fallen to its minimal point
        if (currentYPos <= startingYPos) {
            state = State.DONE;
        }
        // If the hand is still falling
        else {
            // Lower the hand
            currentYPos -= riseSpeed / updatesPerSecond;
            
            // Lower the unit along with the hand
            Point3D textureOffset = heldUnit.getTextureOffset();
            textureOffset.addToY(-riseSpeed / updatesPerSecond);
            heldUnit.setTextureOffset(textureOffset);
        }
    }
    
    private void holding() {
        // Do nothing
    }
    
    public void init() {
        // Prevent the held unit from moving
        heldUnit.setCanMove(false);
        
        // If the target is an enemy
        if (heldUnit.getOwner() != actingUnitOwner) {
            // Prevent the held unit from acting
            heldUnit.setCanAct(false);
        }
        
        // Add the hand as a rendering decoration on the unit
        heldUnit.addBackgroundDecoration(this);
        
        // Start updating me
        notifier.register(this);
        gameFlow.addUpdatable(this);
        
        state = State.RISING;
    }
    
    @Subscribe
    public void listenForEndOfRound(final EndRoundEvent e) {
        roundsRemaining--;
        
        // If the hold has not ended
        if (roundsRemaining > 0) {
            // If the held unit is an enemy
            if (heldUnit.getOwner() != actingUnitOwner) {
                // Deal a small amount of damage to that unit
                final int holdDamage = 1;
                heldUnit.takeDamage(holdDamage);
            }
        }
        // If the hold has ended
        else {
            // Start the hand falling
            state = State.FALLING;
        }
    }
    
    @Override
    public void render(MVP mvp) {
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.scaleM(model, Constants.NO_OFFSET, 1.0f, device.getAspectRatio(), 1.0f);
        final float textureYOffset = -0.3f;
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, textureYOffset, 0.0f);
        
        shader.activate();
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(texture);
        shader.draw();
    }
    
    private void rising(final int updatesPerSecond) {
        // If the hand has risen to its top position
        if (currentYPos >= maxYPos) {
            state = State.HOLDING;
        }
        // If the hand is still rising
        else {
            // Raise the hand
            currentYPos += riseSpeed / updatesPerSecond;
            
            // Raise the unit along with the hand
            Point3D textureOffset = heldUnit.getTextureOffset();
            textureOffset.addToY(riseSpeed / updatesPerSecond);
            heldUnit.setTextureOffset(textureOffset);
        }
    }

    @Override
    public void updateState(final int updatesPerSecond) {
        switch (state) {
            case RISING:
                rising(updatesPerSecond);
                break;
            case HOLDING:
                holding();
                break;
            case FALLING:
                falling(updatesPerSecond);
                break;
            case DONE:
                done();
                break;
            default:
                throw new UnsupportedOperationException("Unknown state!");
        }
    }
}
