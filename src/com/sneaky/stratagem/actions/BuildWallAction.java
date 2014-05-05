package com.sneaky.stratagem.actions;

import scatcat.general.points.GridPoint2D;
import scatcat.graphics.MVP;
import scatcat.map.Battlefield;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.sneaky.stratagem.actions.threats.RadialPassivePattern.RadialPassivePatternFactory;
import com.sneaky.stratagem.match.Match;
import com.sneaky.stratagem.obstacles.MasonsWall;
import com.sneaky.stratagem.units.Unit;

public class BuildWallAction extends Action {
    private static final int actionPointCost = 1;
    
    private final Unit actor;
    
    private static final int chargePointCost = 1;
    
    private final Injector injector;
    
    private static final int range = 1;
    
    @Inject
    public BuildWallAction(
            @Assisted final Unit actor,
            final Injector injector,
            final RadialPassivePatternFactory threatRangeFactory) {
        super(actionPointCost, actor, chargePointCost, injector);
        
        this.actor = actor;
        this.injector = injector;

        setName("Build Wall");
        setThreatRange(threatRangeFactory.create(this));
    }
    
    public interface BuildWallActionFactory {
        BuildWallAction create(Unit actor);
    }
    
    @Override
    public int getRange() { return range; }
    
    @Override
    public void render(MVP mvp) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void performThreatenedAction(Unit actingUnit, GridPoint2D sourceTile, GridPoint2D targetTile) {
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        
        // Orient the wall to face its constructor
        MasonsWall wall = injector.getInstance(MasonsWall.class);
        ActionHelper.orientActor(wall, targetTile, sourceTile);
        
        // Place the wall at the targeted location
        battlefield.getTile(targetTile).get().setOccupant(wall);
       
        // Spend the charge points required to build the wall
        actor.spendChargePoints(chargePointCost);
        
        // Spend the action points required to build the wall //TODO fix this
        Match match = injector.getInstance(Match.class);
        match.spendActionPoints(actionPointCost);
        
        // Set the unit as having acted //TODO fix this
        actor.setHasActed(true);
    }

    @Override
    public void updateState(int updatesPerSecond) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BuildWallAction clone() {
        throw new UnsupportedOperationException();
    }

}
