package com.sneaky.stratagem.units;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import android.opengl.Matrix;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Injector;
import com.sneaky.stratagem.actions.Executable;
import com.sneaky.stratagem.flow.EndRoundEvent;
import com.sneaky.stratagem.match.Player;

import scatcat.effects.StatusMessageAnimation.StatusMessageAnimationFactory;
import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.general.points.Point2D;
import scatcat.general.points.Point3D;
import scatcat.graphics.Color;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.shaders.SimpleTexturedShader;
import scatcat.map.Battlefield;

/**
 * Basic class representing units in the game.
 * @author R. Matt McCann
 */
public abstract class Unit implements RenderableMVP {
    /**********************************************************************************************
     * Rendering Variables
     **********************************************************************************************/
    private List<RenderableMVP> backgroundDecorations = new ArrayList<RenderableMVP>();
    
    private List<RenderableMVP> foregroundDecorations = new ArrayList<RenderableMVP>();
    
    private final int profileTexture;
    
    private Point3D textureOffset = new Point3D(0.0f, 0.0f, 0.0f);
    
    private float textureScale = 1.0f;
    
    private Point2D tileOffset = new Point2D();
    
    private ChargeType abilityChargeType;
    
    private float chargePoints;
    
    private List<Executable> actions = new ArrayList<Executable>(); 
    
    private int apCostOfAttack;
    
    private int apCostOfMovement;
    
    private float attackDamage;
    
    private int attackRange;
    
    private boolean canAct = true;
    
    private boolean canMove = true;
    
    /** Current animation effects on the unit. */
    private Set<Object> effects = new HashSet<Object>();
    
    private int enlistCost;
    
    private final int facingEastTexture;
    
    private final int facingNorthTexture;

    private final int facingSouthTexture;
    
    private final int facingWestTexture;
    
    private boolean hasActed = false;
    private boolean hasMoved = false;
    
    private Heading heading = Heading.NORTH;
    
    private float health;
    
    private float height = 1.0f;
    
    private final Injector injector;
    
    private boolean isStunned = false;
    
    private float maxChargePoints;
    
    private float maxHealth;
    
    private int movementRange;
    
    private String name;
    
    private int numPassivelyAccruedChargePoints = 0;
    
    private Player owner;
    
    private float width = 1.0f;
    
    public enum AttackType {
        PHYSICAL("Physical"),
        FIRE("Fire");
        
        private String value;
        
        private AttackType(final String value) {
            this.value = value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
    
    public enum ChargeType {
        TAKE_DAMAGE("Take Damage");
        
        private String value;
        
        private ChargeType(final String value) {
            this.value = value;
        }
        
        @Override
        public String toString() {
            return value;
        }
    }
    
    /** The options for sprite direction of face. */
    public enum Heading {
        NORTH,
        EAST,
        SOUTH,
        WEST
    }
    
    protected Unit(final Injector injector,
                   final int profileTexture,
                   final int facingNorthTexture,
                   final int facingEastTexture,
                   final int facingSouthTexture,
                   final int facingWestTexture) {
        this.injector = injector;
        this.profileTexture = profileTexture;
        this.facingNorthTexture = facingNorthTexture;
        this.facingEastTexture = facingEastTexture;
        this.facingSouthTexture = facingSouthTexture;
        this.facingWestTexture = facingWestTexture;
        
        EventBus notifier = injector.getInstance(EventBus.class);
        notifier.register(this);
    }
 
    public void addBackgroundDecoration(final RenderableMVP decoration) { 
        backgroundDecorations.add(checkNotNull(decoration));
    }
    
    public void addEffect(final Object effect) { 
        effects.add(effect); 
    }
    
    public void addForegroundDecoration(final RenderableMVP decoration) {
        foregroundDecorations.add(checkNotNull(decoration));
    }
    
    public void attack(final Unit foe) {
        float foeHealth = foe.getHealth();
        foeHealth -= attackDamage;
        foe.setHealth(foeHealth);
        
        // Apply the damage taken action to the target
        StatusMessageAnimationFactory animationFactory = injector.getInstance(StatusMessageAnimationFactory.class);
        foe.addEffect(animationFactory.create(Color.RED, "-" + (int) attackDamage));
    }
    
    public boolean canAct() { return canAct && !hasActed; }
    
    public boolean canMove() { return canMove && !hasMoved; }
    
    @Override
    public Unit clone() {
        Unit clone = injector.getInstance(this.getClass());
        
        cloneGenericProperties(clone);
        
        return clone;
    }
    
    protected void cloneGenericProperties(final Unit clone) {
        clone.abilityChargeType = this.abilityChargeType;
        clone.chargePoints = this.chargePoints;
        clone.apCostOfAttack = this.apCostOfAttack;
        clone.apCostOfMovement = this.apCostOfMovement;
        clone.attackDamage = this.attackDamage;
        clone.attackRange = this.attackRange;
        clone.enlistCost = this.enlistCost;
        clone.heading = this.heading;
        clone.health = this.health;
        clone.height = this.height;
        clone.maxChargePoints = this.maxChargePoints;
        clone.maxHealth = this.maxHealth;
        clone.movementRange = this.movementRange;
        clone.name = this.name;
        clone.owner = this.owner;
        clone.width = this.width;
    }
    
    public final void die() {
        injector.getInstance(Battlefield.class).removeUnit(this);
    }
    
    public void gainChargePoints(final int chargePointsGained) {
        if (chargePoints + chargePointsGained >= maxChargePoints) {
            chargePoints = maxChargePoints;
        } else {
            chargePoints += chargePointsGained;
        }
    }
    
    public void gainHealth(final int healthGained) {
        if (health + healthGained >= maxHealth) {
            health = maxHealth;
        } else {
            health += healthGained;
        }
    }
    
    public final ChargeType getAbilityChargeType() { return abilityChargeType; }
    public final float getChargePoints() { return chargePoints; }
    public final List<Executable> getActions() { return actions; }
    public final int getApCostOfAttack() { return apCostOfAttack; }
    public final int getApCostOfMovement() { return apCostOfMovement; }
    public final int getAttackDamage() { return (int) attackDamage; }
    public final int getAttackRange() { return attackRange; } 
    
    public boolean getCanAct() { return canAct; }
    
    public boolean getCanMove() { return canMove; }
    
    public final Set<Object> getEffects() { return effects; }
    
    public final int getEnlistCost() { return enlistCost; }
    public final float getHealth() { return health; }
    public final float getHeight() { return height; }
    public final float getMaxChargePoints() { return maxChargePoints; }   
    public final float getMaxHealth() { return maxHealth; }
    public final int getMovementRange() { return movementRange; } 
    public final String getName() { return name; }
    public final Player getOwner() { return owner; }
    public final int getProfileTexture() { return profileTexture; }
    
    public Point3D getTextureOffset() { return textureOffset.clone(); }
    
    public Point2D getTileOffset() { return tileOffset; } 
    
    public final float getWidth() { return width; }
    
    public boolean hasActed() { return hasActed; }
    public boolean hasMoved() { return hasMoved; }
    
    public boolean isStunned() { return isStunned; }
    
    @Subscribe
    public void listenForEndOfRound(EndRoundEvent e) {
        // Accrue the passive charge points
        gainChargePoints(numPassivelyAccruedChargePoints);
        
        // Allow me to move and act again
        setHasActed(false);
        setHasMoved(false);
    }
    
    public void removeBackgroundDecoration(final RenderableMVP decoration) {
        backgroundDecorations.remove(decoration);
    }
    
    public void removeForegroundDecoration(final RenderableMVP decoration) {
        foregroundDecorations.remove(decoration);
    }
    
    protected void renderDecorations(final List<RenderableMVP> decorations, final MVP mvp) {
        final Device device = injector.getInstance(Device.class);
        
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.scaleM(model, Constants.NO_OFFSET, textureScale, textureScale, 1.0f);
        Matrix.scaleM(model, Constants.NO_OFFSET, 1.0f, device.getAspectRatio(), 1.0f);
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, 0.0f, -0.1f);
        Matrix.translateM(model, Constants.NO_OFFSET, textureOffset.getX(), textureOffset.getY(), 
                textureOffset.getZ());
        mvp.push(MVP.Type.MODEL, model);
        
        for (RenderableMVP decoration : decorations) {
            decoration.render(mvp);
        }
        
        mvp.pop(MVP.Type.MODEL);
    }
    
    protected void renderEffects(final MVP mvp) {
        Device device = injector.getInstance(Device.class);
        
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.scaleM(model, Constants.NO_OFFSET, 1.0f, device.getAspectRatio(), 1.0f);
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, 0.0f, 0.1f);
        Matrix.translateM(model, Constants.NO_OFFSET, textureOffset.getX(), textureOffset.getY(), 
                textureOffset.getZ());
        mvp.push(MVP.Type.MODEL, model);
        for (Object effect : effects) {
            if (effect instanceof RenderableMVP) {
                ((RenderableMVP) effect).render(mvp);
            }
        }
        mvp.pop(MVP.Type.MODEL);
    }
    
    protected void renderUnit(final MVP mvp) {
        final SimpleTexturedShader shader = injector.getInstance(SimpleTexturedShader.class);
        shader.activate();
        
        // Select the appropriate facing texture
        int textureHandle;
        switch (heading) {
            case NORTH:
                textureHandle = facingNorthTexture;
                break;
            case EAST:
                textureHandle = facingEastTexture;
                break;
            case SOUTH:
                textureHandle = facingSouthTexture;
                break;
            case WEST:
                textureHandle = facingWestTexture;
                break;
            default:
                throw new RuntimeException("Unsupported heading!");
        }
        
        // Shift the unit in tile space based off its offset. This is an interface for abilities
        // causing the unit to move around the map in a strictly animation sense
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        battlefield.moveInTileSpace(model, tileOffset.getX(), tileOffset.getY());
        
        // Scale the unit texture according to the individual units dimensions
        Matrix.scaleM(model, Constants.NO_OFFSET, textureScale, textureScale, 1.0f);
        
        // Adjust for the aspect ratio
        final Device device = injector.getInstance(Device.class);
        Matrix.scaleM(model, Constants.NO_OFFSET, 1.0f, device.getAspectRatio(), 1.0f);
        
        // Adjust the unit texture according to the individual units dimensions
        Matrix.translateM(model, Constants.NO_OFFSET, textureOffset.getX(), textureOffset.getY(), 
                textureOffset.getZ());
        
        // Draw the unit
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(textureHandle);
        shader.draw();
    }
    
    public void removeEffect(final Object effect) {
        effects.remove(effect);
    }
    
    /** {@inheritDoc} */
    @Override
    public void render(final MVP mvp) {
        renderDecorations(backgroundDecorations, mvp);
        renderUnit(mvp);
        renderDecorations(foregroundDecorations, mvp);
        
        // Render the effects currently active on the unit
        renderEffects(mvp);
    }
    
    public void renderAnimations(final MVP mvp) { }
    
    public final void setAbilityChargeType(final ChargeType abilityChargeType) { this.abilityChargeType = abilityChargeType; }
    public final void setChargePoints(final float chargePoints) { this.chargePoints = chargePoints; }
    public final void setActions(final List<Executable> actions) { this.actions = actions; }
    public final void setApCostOfAttack(final int apCostOfAttack) { this.apCostOfAttack = apCostOfAttack; }
    public final void setApCostOfMovement(final int apCostOfMovement) { this.apCostOfMovement = apCostOfMovement; }
    public final void setAttackDamage(final float attackDamage) { this.attackDamage = attackDamage; }
    public final void setAttackRange(final int attackRange) { this.attackRange = attackRange; }
    
    public void setCanAct(final boolean canAct) { this.canAct = canAct; }
    
    public void setCanMove(final boolean canMove) { this.canMove = canMove; }
    
    public final void setEnlistCost(final int enlistCost) { this.enlistCost = enlistCost; }
    public void setHasActed(final boolean hasActed) { this.hasActed = hasActed; }
    public void setHasMoved(final boolean hasMoved) { this.hasMoved = hasMoved; }
    public final void setHeading(final Heading heading) { this.heading = heading; }
    public final void setHealth(final float health) {
        this.health = health;
        
        // If the unit's health has fallen to zero or less, it has died
        if (health <= 0.0f) {
            die();
        }
    }   
    
    public void setIsStunned(boolean isStunned) {
        this.isStunned = isStunned;
    }
    
    public final void setMaxChargePoints(final float maxChargePoints) { this.maxChargePoints = maxChargePoints; }  
    public final void setMaxHealth(final float maxHealth) { this.maxHealth = maxHealth; }
    public final void setMovementRange(final int movementRange) { this.movementRange = movementRange; }    
    public final void setName(final String name) { this.name = name; }
    
    public void setNumPassivelyAccruedChargePoints(int numPoints) {
        this.numPassivelyAccruedChargePoints = numPoints;
    }
    
    public final void setOwner(final Player owner) { this.owner = owner; }
    
    public void setTextureOffset(final Point3D textureOffset) {
        this.textureOffset = textureOffset;
    }
    
    public void setTextureScale(final float textureScale) {
        checkArgument(textureScale > 0.0f, "Texture scale must be greater than 0");
        this.textureScale = textureScale;
    }
     
    public void setTileOffset(final Point2D tileOffset) {
        this.tileOffset = checkNotNull(tileOffset);
    }
    
    public void spendChargePoints(final int points) {
        checkArgument(chargePoints >= points, "Available charge points less than amount attempting to spend!");
        
        chargePoints -= points;
    }
    
    public void takeDamage(final int damage) {
        health -= damage;
        
        StatusMessageAnimationFactory statusFactory = injector.getInstance(StatusMessageAnimationFactory.class);
        addEffect(statusFactory.create(Color.RED, "-" + damage));
        
        if (health <= 0) {
            die();
        }
    }
}
