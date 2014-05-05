package com.sneaky.stratagem.actions.threats;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.inject.Injector;
import com.sneaky.stratagem.graphics.textures.HighlightTexturePack;
import com.sneaky.stratagem.huds.UnitHUD;
import com.sneaky.stratagem.units.Unit;
import com.sneaky.stratagem.units.Unit.Heading;

import scatcat.general.Cleanable;
import scatcat.general.points.GridPoint2D;
import scatcat.input.TileSelectionHandler;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

public abstract class ThreatPattern implements TileSelectionHandler {
    private final ThreatenedActionPerformer actionPerformer;
    
    private Cleanable cleanUpFunction = new Cleanable() {
        @Override
        public void cleanUp() {
            Battlefield battlefield = injector.getInstance(Battlefield.class);
            
            // Remove the attack-potential highlight from the highlighted tiles 
            for (GridPoint2D tile : threatenedTiles) {
                battlefield.getTile(tile).get().setHighlightTexture(Optional.<Integer>absent());
            }
            threatenedTiles.clear();
            
            // Remove the action as a tile pick handler
            battlefield.setActiveAction(Optional.<TileSelectionHandler>absent());
        }    
    };
    
    private final HighlightTexturePack highlightTextures;
    
    private final Injector injector;
    
    private final Set<GridPoint2D> threatenedTiles = new HashSet<GridPoint2D>();
    
    protected ThreatPattern(final ThreatenedActionPerformer actionPerformer,
                            final HighlightTexturePack highlightTextures,
                            final Injector injector) {
        this.actionPerformer = actionPerformer;
        this.highlightTextures = highlightTextures;
        this.injector = injector;
    }
    
    public void applyThreat() {
        Battlefield battlefield = injector.getInstance(Battlefield.class);
        UnitHUD unitHUD = injector.getInstance(UnitHUD.class);
        
        // Retrieve the resources needed to set up the threat
        final Unit        selectedUnit = unitHUD.getActiveUnit();
        final GridPoint2D selectedTile = battlefield.getSelectedTile().get();
        
        // Populate the set of tiles to be threatened
        populateThreatenedTiles(selectedUnit, selectedTile, threatenedTiles);
        
        // Highlight the tiles that are threatened
        for (GridPoint2D tilePoint : threatenedTiles) {
            highlightTextures.setTexture(tilePoint, threatenedTiles);
        }
        
        // Set this threat pattern as a tile pick handler for the battlefield
        battlefield.setActiveAction(Optional.<TileSelectionHandler>of(this));
    }
    
    public void cleanUp() {
        cleanUpFunction.cleanUp();
    }
    
    // Orients the unit to face an action given the start and stop points
    protected void orientActor(final Unit toBeOriented, final GridPoint2D start, final GridPoint2D stop) {
        final int eastInfluence = stop.getRow() - start.getRow();
        final int northInfluence = start.getColumn() - stop.getColumn();
        final int southInfluence = stop.getColumn() - start.getColumn();
        final int westInfluence = start.getRow() - stop.getRow();
        
        int maxInfluence = northInfluence;
        toBeOriented.setHeading(Heading.NORTH);
        if (eastInfluence >= maxInfluence) {
            toBeOriented.setHeading(Heading.EAST);
            maxInfluence = eastInfluence;
        }
        if (southInfluence >= maxInfluence) {
            toBeOriented.setHeading(Heading.SOUTH);
            maxInfluence = southInfluence;
        }
        if (westInfluence >= maxInfluence) {
            toBeOriented.setHeading(Heading.WEST);
            maxInfluence = westInfluence;
        }
    }
    
    @Override
    public boolean handleTileSelection(GridPoint2D tileLocation) {
        final Battlefield battlefield = injector.getInstance(Battlefield.class);
        final Optional<Tile> clickedTile = battlefield.getTile(tileLocation);
        final GridPoint2D activeTilePos = battlefield.getSelectedTile().get();
        final Tile activeTile = battlefield.getTile(activeTilePos).get();
        
        // If the active unit is clicked, clear the attack grid
        if (clickedTile.isPresent() && (clickedTile.get() == activeTile)) {
            cleanUp();
            return true;
        }
        
        // If the selected tile exists
        if (clickedTile.isPresent()) {
            // If the tile is threatened
            for (GridPoint2D threatenedTile : threatenedTiles) {
                if (tileLocation.equals(threatenedTile)) {
                    final UnitHUD unitHUD = injector.getInstance(UnitHUD.class);
                    final Unit actingUnit = unitHUD.getActiveUnit();
                    
                    actionPerformer.performThreatenedAction(actingUnit, activeTilePos, tileLocation);
                    orientActor(actingUnit, activeTilePos, tileLocation);
                    cleanUp();
                    
                    return true;
                }
            }
        }
        
        cleanUp();
        
        return false;
    }
    
    public abstract void populateThreatenedTiles(final Unit selectedUnit,
                                                 final GridPoint2D selectedTile,
                                                 final Set<GridPoint2D> threatenedTiles);

    public void setCleanUpFunction(final Cleanable cleanUpFunction) {
        this.cleanUpFunction = cleanUpFunction;
    }
}
