package com.sneaky.stratagem.actions;

import scatcat.graphics.RenderableMVP;

import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.Optional;
import com.google.inject.Injector;
import com.sneaky.stratagem.actions.threats.ThreatPattern;
import com.sneaky.stratagem.actions.threats.ThreatenedActionPerformer;
import com.sneaky.stratagem.flow.GameFlowController;
import com.sneaky.stratagem.flow.Updatable;
import com.sneaky.stratagem.match.Match;
import com.sneaky.stratagem.units.Unit;

public abstract class Action extends Executable implements RenderableMVP, ThreatenedActionPerformer, Updatable {
    private int actionPointCost;
    
    private Unit actor;
    
    private int chargePointCost;
    
    private final Injector injector;
    
    private boolean isEnabled = false;
    
    private Optional<ThreatPattern> threatRange = Optional.absent();
    
    protected Action(int actionPointCost,
                     Unit actor,
                     int chargePointCost,
                     Injector injector) {
        this.actionPointCost = actionPointCost;
        this.actor = actor;
        this.chargePointCost = chargePointCost;
        this.injector = injector;
    }
    
    @Override
    public void execute() { 
        checkState(threatRange.isPresent(), "Threat range has not been set! Have you called setRange()?");
        
        threatRange.get().applyThreat(); 
    }
    
    public int getActionPointCost() { return actionPointCost; }
    
    public Unit getActor() { return actor; }
    
    public int getChargePointCost() { return chargePointCost; }
    
    protected ThreatPattern getThreatRange() {
        return threatRange.get();
    }
    
    public boolean isEnabled() { return isEnabled; }

    @Override
    public boolean isExecutable() {
        Match match = injector.getInstance(Match.class);
        
        return (!actor.isStunned() &&
                actor.canAct() && 
                (actor.getChargePoints() >= chargePointCost) &&
                (match.getCurrentActionPoints() >= actionPointCost));
                
    }
    
    public void setActionPointCost(int actionPointCost) { this.actionPointCost = actionPointCost; }
    
    public void setActor(Unit actor) { this.actor = actor; }
    
    public void setChargePointcost(int chargePointCost) { this.chargePointCost = chargePointCost; }
    
    public void setIsEnabled(final boolean isEnabled) {
        this.isEnabled = isEnabled;
        
        GameFlowController gameFlow = injector.getInstance(GameFlowController.class);
        if (isEnabled) {
            gameFlow.addUpdatable(this);
        } else {
            gameFlow.removeUpdatable(this);
        }
    }
    
    protected void setThreatRange(final ThreatPattern threatRange) {
        this.threatRange = Optional.of(threatRange);
    }
}
