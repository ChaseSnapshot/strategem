package com.sneaky.stratagem.actions.threats;

import java.util.HashSet;
import java.util.Set;

import scatcat.general.points.GridPoint2D;

import com.google.inject.Injector;
import com.sneaky.stratagem.graphics.textures.HighlightTexturePack;

public abstract class LinearThreatPattern extends ThreatPattern {
    protected LinearThreatPattern(final ThreatenedActionPerformer actionPerformer,
                                  final HighlightTexturePack highlightTextures, 
                                  final Injector injector) {
        super(actionPerformer, highlightTextures, injector);
    }

    /** Returns a set of tile points within the linear pattern. */
    protected Set<GridPoint2D> getTilesInLinearPattern(final GridPoint2D selectedTile,
                                                       final int range) {
        final Set<GridPoint2D> tilesInRange = new HashSet<GridPoint2D>();
       
        for (int colIter = -range; colIter <= range; colIter++) {
            final GridPoint2D tilePos = new GridPoint2D();
            
            tilePos.setColumn(selectedTile.getColumn() + colIter);
            tilePos.setRow(selectedTile.getRow());
            
            tilesInRange.add(tilePos);
        }
        
        for (int rowIter = -range; rowIter < range; rowIter++) {
            if (rowIter == 0) { continue; } // Don't double add the center tile
            
            final GridPoint2D tilePos = new GridPoint2D();
            
            tilePos.setColumn(selectedTile.getColumn());
            tilePos.setRow(selectedTile.getRow() + rowIter);
            
            tilesInRange.add(tilePos);
        };
        
        return tilesInRange;
    }
}
