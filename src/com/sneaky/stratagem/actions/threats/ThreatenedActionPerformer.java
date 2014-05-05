package com.sneaky.stratagem.actions.threats;

import scatcat.general.points.GridPoint2D;

import com.sneaky.stratagem.units.Unit;

public interface ThreatenedActionPerformer {
    int getRange();
    
    void performThreatenedAction(final Unit actingUnit, final GridPoint2D sourceTile, final GridPoint2D targetTile);
}
