package com.sneaky.stratagem.actions.threats;

import java.util.HashSet;
import java.util.Set;

import scatcat.general.points.GridPoint2D;

import com.google.inject.Injector;
import com.sneaky.stratagem.graphics.textures.HighlightTexturePack;

public abstract class RadialThreatPattern extends ThreatPattern {
    protected RadialThreatPattern(final ThreatenedActionPerformer actionPerformer,
                                  final HighlightTexturePack highlightTextures, 
                                  final Injector injector) {
        super(actionPerformer, highlightTextures, injector);
    }

    /** Returns a set of tile points within a radial pattern. */
    protected Set<GridPoint2D> getTilesInRadius(final GridPoint2D selectedTile,
                                                final int radius) {
        final int column = selectedTile.getColumn();
        final int row = selectedTile.getRow();
        
        final Set<GridPoint2D> tilesInRadius = new HashSet<GridPoint2D>();
        for (int rowIter = row - radius; rowIter <= row + radius; rowIter++) {
            for (int colIter = column - Math.abs(Math.abs(row - rowIter) - radius);
                     colIter <= column + Math.abs(Math.abs(row - rowIter) - radius); colIter++) {
                tilesInRadius.add(new GridPoint2D(rowIter, colIter));
            }
        }
        
        return tilesInRadius;
    }
}

