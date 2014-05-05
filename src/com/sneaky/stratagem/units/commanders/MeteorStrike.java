package com.sneaky.stratagem.units.commanders;

import scatcat.general.Constants;
import scatcat.general.points.GridPoint2D;
import scatcat.general.points.Point3D;
import scatcat.graphics.MVP;
import scatcat.graphics.shaders.SimpleTexturedShader;
import scatcat.map.Battlefield;
import scatcat.map.Tile;
import android.opengl.Matrix;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.sneaky.stratagem.actions.Action;
import com.sneaky.stratagem.actions.threats.RadialAttackPattern.RadialAttackPatternFactory;
import com.sneaky.stratagem.battle.BattleRenderer;
import com.sneaky.stratagem.units.Unit;

public class MeteorStrike extends Action {
    private static final int actionPointCost = 1;
    
    private final Unit actor;
    
    private static final int chargePointCost = 10;
    
    private static final int range = 4;
    
    private final int cost = 4;
    private final int damage = 8;
    
    private final float   initialExplosionScale = 0.5f;
    private float         explosionScale = initialExplosionScale;
    private final Point3D initialExplosionPosition = new Point3D(0.0f, 0.0f, 0.0f);
    private Point3D       explosionPosition = initialExplosionPosition.clone();
    private final int     explosionTexture;
    
    private final Injector injector;
    
    private boolean isInFlight = true; 
    
    private final int     meteorTexture;
    private final Point3D initialMovementVector = new Point3D(0.0f, -1.0f, 0.0f);
    private Point3D       movementVector = initialMovementVector.clone();
    private final Point3D initialPosition = new Point3D(0.0f, 1.0f, 0.0f);
    private Point3D       position = initialPosition.clone();
    
    private final SimpleTexturedShader shader;
    
    private Unit target;
    private GridPoint2D targetedTile;
        
    @Inject
    protected MeteorStrike(@Assisted Unit actor,
                           final RadialAttackPatternFactory attackPatternFactory,
                           @Named("ExplosionTexture") final int explosionTexture,
                           final Injector injector,
                           @Named("MeteorTexture") final int meteorTexture,
                           final SimpleTexturedShader shader) {
        super(actionPointCost, actor, chargePointCost, injector);
        
        this.actor = actor;
        this.explosionTexture = explosionTexture;
        this.injector = injector;
        this.meteorTexture = meteorTexture;
        this.shader = shader;
        
        setName("Meteor Strike");
        setThreatRange(attackPatternFactory.create(this));
    }
    
    public interface MeteorStrikeFactory {
        MeteorStrike create(Unit actor);
    }

    @Override
    public MeteorStrike clone() {
        return injector.getInstance(MeteorStrike.class);
    }
    
    @Override
    public int getRange() { return range; }
    
    @Override
    public boolean isExecutable() {
        if (actor.getChargePoints() > cost) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public void performThreatenedAction(Unit actingUnit, final GridPoint2D sourceTile, GridPoint2D targetTile) {
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        
        // Adjust the starting position based off the current zoom level
        final float currentZoom = injector.getInstance(BattleRenderer.class).getCameraZoom();
        
        position.setX(position.getX() / currentZoom);
        position.setY(position.getY() / currentZoom);
        position.setZ(position.getZ() / currentZoom);
        movementVector.setX(movementVector.getX() / currentZoom);
        movementVector.setY(movementVector.getY() / currentZoom);
        movementVector.setZ(movementVector.getZ() / currentZoom);
        
        // Adjust the starting position based off the target tile location
        //position.addToY(-targetTile.getRow() + sourceTile.getRow());
        //position.addToX(-targetTile.getColumn() + sourceTile.getColumn());
        
        targetedTile = targetTile;
        target = battlefield.getTile(targetTile).get().getOccupant();
        if (target == null) {
            return;
        }
        target.addEffect(this);
        
        // Adjust the explosion position based off the target tile location
        //explosionPosition = new Point3D(targetTile.getRow() - sourceTile.getRow(), 
        //        targetTile.getColumn() - sourceTile.getColumn(), 0);
        //battlefield.moveInTileSpace(explosionPosition, targetTile.getRow() - sourceTile.getRow(),
        //        targetTile.getColumn() - targetTile.getColumn());
        //explosionPosition.addToY(-targetTile.getRow() + sourceTile.getRow());
        //explosionPosition.addToX(-targetTile.getColumn() + sourceTile.getColumn());
        
        actor.spendChargePoints(cost);
        
        setIsEnabled(true); //TEMP
    }
    
    @Override
    public void render(MVP mvp) {
        // If the meteorite hasn't reached its impact location yet
        if (isInFlight) {
            // Draw the meteorite
            final float[] model = mvp.peekCopy(MVP.Type.MODEL);
            Matrix.translateM(model, Constants.NO_OFFSET, position.getX(), position.getY(), position.getZ());
            Matrix.scaleM(model, Constants.NO_OFFSET, 1.0f, 2.0f, 1.0f);
            Matrix.rotateM(model, Constants.NO_OFFSET, -90.0f, 0.0f, 0.0f, 1.0f);
            Matrix.scaleM(model, Constants.NO_OFFSET, 0.5f, 0.5f, 1.0f);
            shader.setMVPMatrix(mvp.collapseM(model));
            shader.setTexture(meteorTexture);
            shader.draw();
        } 
        else {
            // Draw the explosion
            final float[] model = mvp.peekCopy(MVP.Type.MODEL);
            injector.getInstance(Battlefield.class).moveInTileSpace(model, (int) explosionPosition.getX(), 
                    (int) explosionPosition.getY());
            Matrix.scaleM(model, Constants.NO_OFFSET, 1.0f, 0.5f, 1.0f);
            Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, 0.4f, 0.1f);
            Matrix.scaleM(model, Constants.NO_OFFSET, explosionScale, explosionScale, 1.0f);
            shader.setMVPMatrix(mvp.collapseM(model));
            shader.setTexture(explosionTexture);
            shader.draw();
        }
    }

    @Override
    public void updateState(int updatesPerSecond) {
        if (isInFlight) {
            final float timeToImpact = 1.5f;
            
            position.addToX(movementVector.getX() / timeToImpact / (float) updatesPerSecond);
            position.addToY(movementVector.getY() / timeToImpact / (float) updatesPerSecond);
            position.addToZ(movementVector.getZ() / timeToImpact / (float) updatesPerSecond);
            
            // If the meteorite has reached its impact destination
            if (position.getY() <= 0.0f) {
                isInFlight = false;
            }
        } else {
            final float explosionTime = 2.0f;
            final float largestScale = 3.0f;
            
            explosionScale *= 1.0f + largestScale / initialExplosionScale / explosionTime / (float) updatesPerSecond;
            
            // If the meteorite explosion has finished
            if (explosionScale >= largestScale) {
                // Reset to the original state
                setIsEnabled(false);
                
                // Reset the action
                isInFlight = true;
                explosionScale = initialExplosionScale;
                movementVector = initialMovementVector.clone();
                position = initialPosition.clone();
                
                // Apply the direct impact damage to the target
                target.removeEffect(this);
                target.takeDamage(damage);
                
                // Apply the secondary damage to the adjacent units
                Battlefield battlefield = injector.getInstance(Battlefield.class);
                for (int rowIter = -1; rowIter <= 1; rowIter++) {
                    for (int colIter = -1; colIter <= 1; colIter++) {
                        // If its the direct impact site, don't apply secondary damage
                        if ((rowIter == 0) && (colIter == 0)) { continue; }
                        
                        GridPoint2D tilePos = new GridPoint2D(targetedTile.getRow() + rowIter,
                                targetedTile.getColumn() + colIter);
                        Optional<Tile> tile = battlefield.getTile(tilePos);
                        
                        // If the tile doesn't exist, move on
                        if (!tile.isPresent()) { continue; }
                        
                        // If the tile doesn't contain a unit, move on
                        if (!tile.get().hasOccupant()) { continue; }
                        
                        // If the secondary target is an enemy, apply the secondary damage
                        Unit secondaryTarget = tile.get().getOccupant();
                        if (secondaryTarget.getOwner() != actor.getOwner()) {
                            secondaryTarget.takeDamage(damage / 2);
                        }
                    }
                }
            }
        }
    }
}
