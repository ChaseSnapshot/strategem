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

public class RadialMovementPattern extends RadialThreatPattern {
    private final Injector injector;
    
    private final ThreatenedActionPerformer action;
    
    @Inject
    protected RadialMovementPattern(@Assisted final ThreatenedActionPerformer action,
                                    @Named("Move") final HighlightTexturePack highlightTextures,
                                    final Injector injector) {
        super(action, highlightTextures, injector);
        
        this.action = action;
        this.injector = injector;
    }
    
    public interface RadialMovementPatternFactory {
        RadialMovementPattern create(ThreatenedActionPerformer action);
    }
    
    @Override
    public void populateThreatenedTiles(final Unit selectedUnit, 
                                           final GridPoint2D selectedTile, 
                                           final Set<GridPoint2D> threatenedTiles) {
        // Get the tiles within the unit's movement range
        final Set<GridPoint2D> tilesInRadius = getTilesInRadius(selectedTile, action.getRange());
        
        // Add the tiles that can be moved to
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        for (GridPoint2D tileInRadius : tilesInRadius) {
            Optional<Tile> tile = battlefield.getTile(tileInRadius);
            
            // If the tile doesn't exist, don't highlight it
            if (!tile.isPresent()) {
                continue;
            } 
            // If the tile has an occupant and its not the moving unit itself, don't highlight it
            else if (tile.get().hasOccupant() && (tile.get().getOccupant() != selectedUnit)) {
                continue;
            }
                         
            threatenedTiles.add(tileInRadius);
        }
    }
}
