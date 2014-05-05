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

public class RadialAttackPattern extends RadialThreatPattern {
    private final ThreatenedActionPerformer attackAction;
    
    private final Injector injector;
    
    @Inject
    protected RadialAttackPattern(@Assisted final ThreatenedActionPerformer attackAction,
                                  @Named("Attack") final HighlightTexturePack highlightTextures,
                                  final Injector injector) {
        super(attackAction, highlightTextures, injector);
        
        this.attackAction = attackAction;
        this.injector = injector;
    }
    
    public interface RadialAttackPatternFactory {
        RadialAttackPattern create(ThreatenedActionPerformer attackAction);
    }
    
    @Override
    public void populateThreatenedTiles(final Unit selectedUnit, 
                                           final GridPoint2D selectedTile, 
                                           final Set<GridPoint2D> threatenedTiles) {
        // Get the tiles within the action's attack range
        final Set<GridPoint2D> tilesInRadius = getTilesInRadius(selectedTile, attackAction.getRange());
        
        // Add the tiles that can be moved to
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        for (GridPoint2D tileInRadius : tilesInRadius) {
            Optional<Tile> tile = battlefield.getTile(tileInRadius);
            
            // If the tile doesn't exist, don't highlight it
            if (!tile.isPresent()) {
                continue;
            } 
            // If the tile occupant is on the same team, don't highlight it
            else if (tile.get().hasOccupant()) {
                Unit occupant = tile.get().getOccupant();
                
                if (occupant.getOwner() == selectedUnit.getOwner()) {
                    continue;
                }
            }
                         
            threatenedTiles.add(tileInRadius);
        }
    }
}
