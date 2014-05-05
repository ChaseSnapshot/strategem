package com.sneaky.stratagem.actions.threats;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Optional;
import com.sneaky.stratagem.units.Unit;

import scatcat.general.points.GridPoint2D;
import scatcat.map.Battlefield;
import scatcat.map.BattlefieldHelper;
import scatcat.map.Tile;

public abstract class ThreatPatternHelper {
    /**
     * Retrieves the grid points in a square pattern around the focal point.
     * 
     * @param focalPoint Point around which to retrieve the square pattern. Must not be null.
     * @param radius Must be a positive integer.
     * @return Grid points in pattern.
     */
    public static Set<GridPoint2D> getTilePointsInSquarePattern(
            final GridPoint2D focalPoint,
            final int radius) {
        checkArgument(focalPoint != null, "Focal point must not be null!");
        checkArgument(radius > 0, "Radius must be a positive integer");
        
        final int column = focalPoint.getColumn();
        final int row = focalPoint.getRow();
        
        final Set<GridPoint2D> tilesInRadius = new HashSet<GridPoint2D>();
        for (int rowIter = row - radius; rowIter <= row + radius; rowIter++) {
            for (int colIter = column - radius; colIter <= column + radius; colIter++) {
                tilesInRadius.add(new GridPoint2D(rowIter, colIter));
            }
        }
        
        return tilesInRadius;
    }
    
    /**
     * Retrieves the tiles in a square pattern around the focal point.
     * 
     * @param battlefield Battlefield from which to retrieve the tiles. Must not be null.
     * @param focalPoint Tile around which to retrieve the square pattern. Must not be null.
     * @param radius Must be a positive integer.
     * @return Tiles in pattern.
     */
    public static Set<Tile> getTilesInSquarePattern(
            final Battlefield battlefield,
            final GridPoint2D focalPoint,
            final int radius) {
        checkArgument(battlefield != null, "Battlefield must not be null!");
        checkArgument(focalPoint != null, "Focal point must not be null!");
        
        Set<GridPoint2D> tilesInRadius = getTilePointsInSquarePattern(focalPoint, radius);
        
        Set<Tile> tiles = new HashSet<Tile>();
        for (GridPoint2D tilePoint : tilesInRadius) {
            Optional<Tile> tile = battlefield.getTile(tilePoint);
            
            if (tile.isPresent()) {
                tiles.add(tile.get());
            }
        }
        
        return tiles;
    }
    
    /**
     * Retrieves the tiles in a square pattern around the focal unit.
     * 
     * @param battlefield Battlefield from which to retrieve the tiles. Must not be null.
     * @param focalUnit Unit around which to retrieve the square pattern. Must not be null.
     * Must exist on the battlefield.
     * @param radius Must be a positive integer.
     * @return Tiles in pattern.
     */
    public static Set<Tile> getTilesInSquarePattern(
            final Battlefield battlefield,
            final Unit focalUnit,
            final int radius) {
        checkArgument(battlefield != null, "Battlefield must not be null!");
        checkArgument(focalUnit != null, "Focal unit must not be null!");
        
        // Retrieve the tile the focal unit is occupying
        Optional<GridPoint2D> focalPoint = BattlefieldHelper.getTile(battlefield, focalUnit);
        checkState(focalPoint.isPresent(), "The focal unit must exist on the battlefield!");
        
        return getTilesInSquarePattern(battlefield, focalPoint.get(), radius);
    }
}
