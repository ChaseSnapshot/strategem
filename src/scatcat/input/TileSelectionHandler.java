package scatcat.input;

import scatcat.general.points.GridPoint2D;

public interface TileSelectionHandler {
    /**
     * Handles the tile selection event.
     * 
     * @param tileLocation Location of the selected tile.
     * @return Whether or not the tile selection event was handled.
     */
    boolean handleTileSelection(final GridPoint2D tileLocation);
}
