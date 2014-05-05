package com.sneaky.stratagem.units;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.sneaky.stratagem.R;
import com.sneaky.stratagem.graphics.textures.TextureManager;
import com.sneaky.stratagem.graphics.textures.UnitTexturePack;
import com.sneaky.stratagem.graphics.textures.UnitTexturePack.UnitTexturePackFactory;
import com.sneaky.stratagem.units.Unit.AttackType;
import com.sneaky.stratagem.units.Unit.ChargeType;
import com.sneaky.stratagem.units.commanders.Commander;


/**
 * Factory pattern that abstracts away the details of constructing various types of units.
 * 
 * @author R. Matt McCann
 */
public final class UnitFactory {
    private final Injector injector;
    
    private final UnitTexturePackFactory unitTexturePackFactory;
    
    /** Possible units that can be constructed. */
    public enum Type {
        ELEMENTAL,
        GOBLIN,
        KNIGHT, 
        OGRE,
        RED_KNIGHT,
        SAGE,
        WIZARD
    }
    
    /** Guice injection constructor. */
    @Inject
    protected UnitFactory(final Injector injector,
                          final UnitTexturePackFactory unitTexturePackFactory) {
        this.injector = injector;
        this.unitTexturePackFactory = unitTexturePackFactory;
    }
    
    /**
     * Primary construction function. Constructs the request unit type.
     * 
     * @param type Type of unit to be constructed.
     * @return Constructed unit.
     */
    public Unit create(final Type type) {
        Unit unit;
        
        /*switch (type) {
            case ELEMENTAL:
                Unit elemental = injector.getInstance(Unit.class);
                elemental.setName("Elemental");
                elemental.setTexturePack(fetchTexturePack("UNIT.ELEMENTAL", R.drawable.elemental_east,
                        R.drawable.elemental_north, R.drawable.elemental_east, R.drawable.elemental_south,
                        R.drawable.elemental_west));
                elemental.setAbilityChargeType(ChargeType.TAKE_DAMAGE);
                elemental.setAbilityName("EARTHQUAKE");
                elemental.setAbilityPower(10);
                elemental.setApCostOfAttack(2);
                elemental.setApCostOfMovement(1);
                elemental.setAttackDamage(2);
                elemental.setAttackRange(3);
                elemental.setAttackType(AttackType.PHYSICAL);
                elemental.setEnlistCost(2);
                elemental.setHealth(5);
                elemental.setMaxAbilityPower(10);
                elemental.setMaxHealth(5);
                elemental.setMovementRange(1);
                unit = elemental;
                break;
            case GOBLIN:
                Unit goblin = injector.getInstance(Unit.class);
                goblin.setName("Goblin");
                goblin.setTexturePack(fetchTexturePack("UNIT.GOBLIN", R.drawable.goblin_east,
                        R.drawable.goblin_east, R.drawable.goblin_east, R.drawable.goblin_east,
                        R.drawable.goblin_east));
                goblin.setAbilityChargeType(ChargeType.TAKE_DAMAGE);
                goblin.setAbilityName("EARTHQUAKE");
                goblin.setAbilityPower(10);
                goblin.setApCostOfAttack(2);
                goblin.setApCostOfMovement(1);
                goblin.setAttackDamage(2);
                goblin.setAttackRange(3);
                goblin.setAttackType(AttackType.PHYSICAL);
                goblin.setEnlistCost(1);
                goblin.setHealth(5);
                goblin.setMaxAbilityPower(10);
                goblin.setMaxHealth(5);
                goblin.setMovementRange(1);
                unit = goblin;
                break;
            case KNIGHT:
                Unit knight = injector.getInstance(Unit.class);
                knight.setName("Foot Soldier");
                knight.setTexturePack(fetchTexturePack("UNIT.KNIGHT", R.drawable.knight_profile, 
                        R.drawable.knight_north, R.drawable.knight_east, R.drawable.knight_south, 
                        R.drawable.knight_west));
                knight.setAbilityChargeType(ChargeType.TAKE_DAMAGE);
                knight.setAbilityName("EARTHQUAKE");
                knight.setAbilityPower(3);
                knight.setApCostOfAttack(2);
                knight.setApCostOfMovement(1);
                knight.setAttackDamage(3);
                knight.setAttackRange(1);
                knight.setAttackType(AttackType.PHYSICAL);
                knight.setHealth(10);
                knight.setMaxAbilityPower(3);
                knight.setMaxHealth(10);
                knight.setMovementRange(2);
                knight.setEnlistCost(1);
                unit = knight;
                break;
            case SAGE:
                Unit sage = injector.getInstance(Unit.class);
                sage.setName("Sage");
                sage.setTexturePack(fetchTexturePack("UNIT.SAGE", R.drawable.sage_east,
                        R.drawable.sage_north, R.drawable.sage_east, R.drawable.sage_south,
                        R.drawable.sage_west));
                sage.setAbilityChargeType(ChargeType.TAKE_DAMAGE);
                sage.setAbilityName("EARTHQUAKE");
                sage.setAbilityPower(10);
                sage.setApCostOfAttack(2);
                sage.setApCostOfMovement(1);
                sage.setAttackDamage(2);
                sage.setAttackRange(3);
                sage.setAttackType(AttackType.PHYSICAL);
                sage.setEnlistCost(2);
                sage.setHealth(5);
                sage.setMaxAbilityPower(10);
                sage.setMaxHealth(5);
                sage.setMovementRange(1);
                unit = sage;
                break;
            case WIZARD:
                Unit wizard = injector.getInstance(Unit.class);
                wizard.setName("Hullabaloo");
                wizard.setTexturePack(fetchTexturePack("UNIT.WIZARD", R.drawable.blue_wizard_east,
                        R.drawable.blue_wizard_north, R.drawable.blue_wizard_east, 
                        R.drawable.blue_wizard_south, R.drawable.blue_wizard_west));
                wizard.setAbilityChargeType(ChargeType.TAKE_DAMAGE);
                wizard.setAbilityName("EARTHQUAKE");
                wizard.setAbilityPower(10);
                wizard.setApCostOfAttack(2);
                wizard.setApCostOfMovement(1);
                wizard.setAttackDamage(2);
                wizard.setAttackRange(3);
                wizard.setAttackType(AttackType.PHYSICAL);
                wizard.setHealth(5);
                wizard.setMaxAbilityPower(10);
                wizard.setMaxHealth(5);
                wizard.setMovementRange(1);
                unit = wizard;
                break;
            case RED_KNIGHT:
                unit = createRedKnight();
                break;
            default:
                throw new RuntimeException("Unhandled unit type!");
        }
        
        unit.setFactoryType(type);
        */
        return null;
        //return unit;
    };
    
    /** Handles the construction of Mr. Red Knight. */
    private Unit createRedKnight() {
        Commander redKnight = injector.getInstance(Commander.class);
        
        /*redKnight.setTexturePack(fetchTexturePack("UNIT.REDKNIGHT", 
                R.drawable.commander_red_knight_profile, R.drawable.commander_red_knight_north,
                R.drawable.commander_red_knight_east, R.drawable.commander_red_knight_south,
                R.drawable.commander_red_knight_west));
        redKnight.setAbilityChargeType(ChargeType.TAKE_DAMAGE);
        redKnight.setAbilityName("EARTHQUAKE");
        redKnight.setAttackType(AttackType.PHYSICAL);
        redKnight.setName("The Red Knight");
        redKnight.setAbilityPower(5);
        redKnight.setMaxAbilityPower(5);
        redKnight.setApCostOfAttack(1);
        redKnight.setAttackDamage(3);
        redKnight.setAttackRange(1);
        redKnight.setHealth(10);
        redKnight.setMaxHealth(10);
        redKnight.setMovementRange(1);*/
        
        return redKnight;
    }
    
    /**
     * Retrieves the specified texture pack from the texture manager. If the 
     * texture pack has not be registered yet, creates and registers the texture
     * pack.
     * 
     * @param id ID of the texture pack
     * @param profileImage Android resource handle for the unit profile texture.
     * @param northImage Android resource handle for the north facing unit texture.
     * @param eastImage " " " east " " "
     * @param southImage " " " south " " " 
     * @param westImage " " " west " " "
     * @return Requested unit texture pack.
     */
    private UnitTexturePack fetchTexturePack(final String id,
                                             final int profileImage,
                                             final int northImage,
                                             final int eastImage,
                                             final int southImage,
                                             final int westImage) {
        UnitTexturePack texturePack;
        
        TextureManager manager = TextureManager.getInstance();
        if (!manager.isRegistered(id)) {
            texturePack = unitTexturePackFactory.create(profileImage, northImage, eastImage, southImage, westImage);
            manager.register(id, texturePack);
        } else {
            texturePack = (UnitTexturePack) manager.getTexturePack(id);
        }
        manager.incrementRefCount(id);
        
        return texturePack;
    }
}
