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

public class LinearAttackPattern extends LinearThreatPattern {
    private final ThreatenedActionPerformer actionPerformer;
    
    private final Injector injector;
    
    @Inject
    protected LinearAttackPattern(@Assisted final ThreatenedActionPerformer actionPerformer,
                                  @Named("Attack") final HighlightTexturePack highlightTextures,
                                  final Injector injector) {
        super(actionPerformer, highlightTextures, injector);
        
        this.actionPerformer = actionPerformer;
        this.injector = injector;
    }
    
    public interface LinearAttackPatternFactory {
        LinearAttackPattern create(ThreatenedActionPerformer actionPerformer);
    }
    
    @Override
    public void populateThreatenedTiles(final Unit selectedUnit,
                                           final GridPoint2D selectedTile, 
                                           final Set<GridPoint2D> threatenedTiles) {
        final Set<GridPoint2D> inRangeTiles 
                = getTilesInLinearPattern(selectedTile, actionPerformer.getRange());
        
        // For each potentially threatened tile
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        for (GridPoint2D tilePos : inRangeTiles) {
            final Optional<Tile> tile = battlefield.getTile(tilePos);
            
            // If the tile does not exist
            if (!tile.isPresent()) {
                // Do not threaten it
                continue;
            }
            // If the potentially threatened tile is the actor's tile
            else if (tilePos.equals(selectedTile)) {
                // Do not threaten it
                continue;
            }
            // Otherwise
            else {
                // Threaten the tile
                threatenedTiles.add(tilePos);
            }
        }
    }
}
