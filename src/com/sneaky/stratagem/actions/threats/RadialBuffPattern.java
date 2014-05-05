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

/**
 * Threat pattern that only allows targeting friendly units.
 * 
 * @author R. Matt McCann
 */
public class RadialBuffPattern extends RadialThreatPattern {
    private final ThreatenedActionPerformer action;
    
    private final Injector injector;
    
    @Inject
    protected RadialBuffPattern(
            @Assisted final ThreatenedActionPerformer action,
            @Named("BuffAction") final HighlightTexturePack highlightTextures, 
            final Injector injector) {
        super(action, highlightTextures, injector);
        
        this.action = action;
        this.injector = injector;
    }

    public interface RadialBuffPatternFactory {
        RadialBuffPattern create(ThreatenedActionPerformer action);
    }
    
    @Override
    public void populateThreatenedTiles(
            final Unit selectedUnit,
            final GridPoint2D selectedTile, 
            final Set<GridPoint2D> threatenedTiles) {
        // Get the tiles within the action's range
        final Set<GridPoint2D> tilesInRadius = getTilesInRadius(selectedTile, action.getRange());
        
        // For each tile in range
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        for (GridPoint2D tileInRadius : tilesInRadius) {
            Optional<Tile> tile = battlefield.getTile(tileInRadius);
            
            // If the tile doesn't exist, don't highlight it
            if (!tile.isPresent()) {
                continue;
            }
            // If the tile has an occupant that isn't on the same team, don't highlight it
            if (tile.get().hasOccupant() && 
                    tile.get().getOccupant().getOwner() != selectedUnit.getOwner()) {
                continue;
            }
            
            threatenedTiles.add(tileInRadius);
        }
    }
}
