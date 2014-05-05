package com.sneaky.stratagem.actions;

import static com.google.common.base.Preconditions.*;
import com.google.common.base.Optional;
import com.sneaky.stratagem.units.Unit;
import com.sneaky.stratagem.units.Unit.Heading;

import scatcat.general.points.GridPoint2D;
import scatcat.map.Battlefield;
import scatcat.map.Tile;

public class ActionHelper {
    /** This is a utility class, so prevent its instantiation. */
    private ActionHelper() { }
    
    public static GridPoint2D calcDirectionVector(final GridPoint2D sourceTile,
                                                  final GridPoint2D targetTile) {
        if (targetTile.getColumn() > sourceTile.getColumn()) {
            return new GridPoint2D(0, 1);
        } else if (targetTile.getColumn() < sourceTile.getColumn()) {
            return new GridPoint2D(0, -1);
        } else if (targetTile.getRow() > sourceTile.getRow()) {
            return new GridPoint2D(1, 0);
        } else if (targetTile.getRow() < sourceTile.getRow()) {
            return new GridPoint2D(-1, 0);
        } else {
            throw new RuntimeException("Unexpected directionality relationship between source and target!");
        }
    }
    
    /**
     * Determines if the next tile is occupiable.
     * 
     * @param battlefield Battlefield containing the tiles. Must not be null.
     * @param nextTilePos The tile to determine if occupiable. Must not be null.
     * @return Whether or not the next tile is occupiable.
     */
    public static boolean checkIfNextTileIsOccupiable(final Battlefield battlefield, final GridPoint2D nextTilePos) {
        checkArgument(battlefield != null, "Battlefield must not be null!");
        checkArgument(nextTilePos != null, "NextTilePos must not be null!");
        
        // If the next tile is missing, don't occupy it
        Optional<Tile> nextTile = battlefield.getTile(nextTilePos);
        if (!nextTile.isPresent()) {
            return false;
        }
        // If the next tile exists, but is occupied
        else if (nextTile.get().hasOccupant()) {
            return false;
        }
        // If the next tile exists and is emptied, let the bashed unit slide into it
        else {
            return true;
        }
    }
    
    /**
     * Determines if the next tile is occupiable.
     * 
     * @param battlefield Battlefield containing the tiles. Must not be null.
     * @param nextTilePos The tile to determine if occupiable. Must not be null.
     * @param range The range of the action. Must be greater than 0.
     * @param startingPos The tile the action started at. Must not be null.
     * @return Whether or not the next tile is occupiable.
     */
    public static boolean checkIfNextTileIsOccupiable(final Battlefield battlefield, 
                                                      final GridPoint2D nextTilePos,
                                                      final int range,
                                                      final GridPoint2D startingPos) {
        checkArgument(battlefield != null, "Battlefield must not be null!");
        checkArgument(nextTilePos != null, "NextTilePos must not be null!");
        checkArgument(range > 0, "Range must be greater than 0.");
        checkArgument(startingPos != null, "StartingPos must not be null!");
        
        boolean isNextTileOccupiable = checkIfNextTileIsOccupiable(battlefield, nextTilePos);
        if (!isNextTileOccupiable) { return false; }
        
        // Check if the next tile is within the action range
        GridPoint2D distance = nextTilePos.minus(startingPos);
        return (Math.abs(distance.getRow()) <= range && Math.abs(distance.getColumn()) <= range);
    }
    
    /**
     * Orients the acting unit based off the directionality of the action.
     * 
     * @param toBeOriented The acting unit to be oriented. Must not be null.
     * @param start The tile where the unit was located when acting. Must not be null.
     * @param stop The tile where the action occurred. Must not be null. 
     */
    public static void orientActor(final Unit toBeOriented,
                                   final GridPoint2D start,
                                   final GridPoint2D stop) {
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
}
