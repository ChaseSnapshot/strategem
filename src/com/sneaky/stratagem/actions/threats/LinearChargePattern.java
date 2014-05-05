package com.sneaky.stratagem.actions.threats;

import java.util.Set;

import scatcat.general.points.GridPoint2D;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;
import com.sneaky.stratagem.graphics.textures.HighlightTexturePack;
import com.sneaky.stratagem.units.Unit;

public class LinearChargePattern extends LinearThreatPattern {
    private final ThreatenedActionPerformer actionPerformer;
    
    private final Injector injector;
    
    @Inject
    protected LinearChargePattern(@Assisted final ThreatenedActionPerformer actionPerformer,
                                  @Named("Attack") final HighlightTexturePack highlightTextures, 
                                  final Injector injector) {
        super(actionPerformer, highlightTextures, injector);
       
        this.actionPerformer = actionPerformer;
        this.injector = injector;
    }
    
    public interface LinearChargePatternFactory {
        LinearChargePattern create(ThreatenedActionPerformer actionPerformer);
    }

    /** {@inheritDoc} */
    @Override
    public void populateThreatenedTiles(final Unit selectedUnit,
                                           final GridPoint2D selectedTile, 
                                           final Set<GridPoint2D> threatenedTiles) {
        
       
        // Determine how far up the row linear path is threatened 
        for (int rowIter = 1; rowIter <= actionPerformer.getRange(); rowIter++) {
            // Decide if the tile is threatened
            boolean continueProcessing = handleTileThreat(selectedUnit, 
                    threatenedTiles, selectedTile.plus(new GridPoint2D(rowIter, 0)));
            
            // If the line is done being processed
            if (!continueProcessing) { break; }
        }
        
        // Determine how far down the row linear path is threatened 
        for (int rowIter = -1; rowIter >= -actionPerformer.getRange(); rowIter--) {
            // Decide if the tile is threatened
            boolean continueProcessing = handleTileThreat(selectedUnit, 
                    threatenedTiles, selectedTile.plus(new GridPoint2D(rowIter, 0)));
            
            // If the line is done being processed
            if (!continueProcessing) { break; }
        }
        
        // Determine how far up the column linear path is threatened
        for (int colIter = 1; colIter <= actionPerformer.getRange(); colIter++) {
            // Decide if the tile is threatened
            boolean continueProcessing = handleTileThreat(selectedUnit, 
                    threatenedTiles, selectedTile.plus(new GridPoint2D(0, colIter)));
            
            // If the line is done being processed
            if (!continueProcessing) { break; }
        }
        
        // Determine how far down the column linear path is threatened
        for (int colIter = -1; colIter >= -actionPerformer.getRange(); colIter--) {
            // Decide if the tile is threatened
            boolean continueProcessing = handleTileThreat(selectedUnit, 
                    threatenedTiles, selectedTile.plus(new GridPoint2D(0, colIter)));
            
            // If the line is done being processed
            if (!continueProcessing) { break; }
        }
    }
    
    /**
     * Handles deciding whether or not a tile is threatened,
     * 
     * @param selectedUnit Unit performing the action. Must not be null.
     * @param threatenedTiles Set to add tile position to if it is threatened
     * @param tilePos Tile to inspect. Must not be null.
     * @return 
     * true - continue processing this line
     * false - stop processing this line
     */
    private boolean handleTileThreat(final Unit selectedUnit,
                                     final Set<GridPoint2D> threatenedTiles,
                                     final GridPoint2D tilePos) {
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        final Optional<Tile> tile = battlefield.getTile(tilePos);
        
        // If this tile does not exist, don't mark it as threatened or any after
        if (!tile.isPresent()) {
            return false;
        }            
        // If this tile does not have an occupant, mark it as threatened
        else if (!tile.get().hasOccupant()) {
            threatenedTiles.add(tilePos);
            return true;
        }
        // If this tile has an enemy occupant, mark it as threatened but no more in the line
        else if (tile.get().getOccupant().getOwner() != selectedUnit.getOwner()) {
            threatenedTiles.add(tilePos);
            return false;
        }
        // If this tile has a friend occupant, don't mark it or any after
        else {
            return false;
        }
    }
}
