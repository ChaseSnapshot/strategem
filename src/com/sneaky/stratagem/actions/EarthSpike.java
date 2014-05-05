package com.sneaky.stratagem.actions;

import android.opengl.Matrix;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.sneaky.stratagem.flow.GameFlowController;
import com.sneaky.stratagem.flow.Updatable;
import com.sneaky.stratagem.match.Player;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.points.GridPoint2D;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.shaders.SimpleTexturedShader;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

public class EarthSpike implements RenderableMVP, Updatable {
    private final Battlefield battlefield;
    
    private float currentYPos;
    
    private final int damage;
    
    private final Device device;
    
    private final float fallSpeed = 1.0f;
    
    private final GameFlowController gameFlow;
    
    private final float minYPos = -0.7f;
    
    private final GridPoint2D location;
    
    private final float maxYPos = 0.3f;
    
    private final Player owner;
    
    private final float riseSpeed = 4.0f;
    
    private final SimpleTexturedShader shader;
    
    private final GridPoint2D spikeVector;
    
    private State state = State.RISING;
    
    private final int texture;
    
    private enum State {
        RISING, FALLING
    }
    
    @Inject
    public EarthSpike(final Battlefield battlefield,
                      @Assisted("damage") final int damage,
                      final Device device,
                      final GameFlowController gameFlow,
                      @Assisted("location") final GridPoint2D location,
                      final Player owner,
                      final SimpleTexturedShader shader,
                      @Assisted("spikeVector") final GridPoint2D spikeVector,
                      @Named("EarthSpikeTexture") final int texture) {
        this.battlefield = battlefield;
        this.currentYPos = minYPos;
        this.damage = damage;
        this.device = device;
        this.gameFlow = gameFlow;
        this.location = location;
        this.owner = owner;
        this.shader = shader;
        this.spikeVector = spikeVector;
        this.texture = texture;
        
        // Start updating me
        gameFlow.addUpdatable(this);
    }
    
    public interface EarthSpikeFactory {
        EarthSpike create(@Assisted("damage") int damage, 
                          @Assisted("location") GridPoint2D location, 
                          @Assisted("spikeVector") GridPoint2D spikeVector);
    }
    
    private void fall(int updatesPerSecond) {
        // If the spike has dropped to its minimal point
        if (currentYPos <= minYPos) {
            final Tile occupiedTile = battlefield.getTile(location).get();
            
            // Remove me from my tile
            occupiedTile.removeEffect(this);
            
            // Stop me from being updated
            gameFlow.removeUpdatable(this);
        }
        // If the spike is still falling
        else {
            currentYPos -= fallSpeed / updatesPerSecond;
        }
    }
    
    @Override
    public void render(MVP mvp) {
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.scaleM(model, Constants.NO_OFFSET, 1.0f, device.getAspectRatio(), 1.0f);
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, currentYPos, 0.6f);
        
        shader.activate();
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(texture);
        shader.draw();
    }

    private void rise(int updatesPerSecond) {
        // If the spike has risen to its maximal point
        if (currentYPos >= maxYPos) {
            final Tile occupiedTile = battlefield.getTile(location).get();
            final Optional<Tile> nextTile = battlefield.getTile(location.plus(spikeVector));
            
            // If the tile in which the spike is rising has an enemy occupant
            if (occupiedTile.hasOccupant() && (occupiedTile.getOccupant().getOwner() != owner)) {
                // Damage the enemy occupant
                occupiedTile.getOccupant().takeDamage(damage);
            }
            
            // If the next tile in the spike vector exists
            if (nextTile.isPresent()) {
                EarthSpike spike = new EarthSpike(battlefield, damage, device, gameFlow, 
                        location.plus(spikeVector), owner, shader, spikeVector, texture);
                
                // Start spiking the next tile
                nextTile.get().addEffect(spike);
            }
            
            // Set the spike in the falling state
            state = State.FALLING;
        }
        // If the spike is still rising
        else {
            currentYPos += riseSpeed / updatesPerSecond;
        }
    }
    
    @Override
    public void updateState(int updatesPerSecond) {
        if (state == State.RISING) {
            rise(updatesPerSecond);
        } else if (state == State.FALLING) {
            fall(updatesPerSecond);
        }
    }
}
