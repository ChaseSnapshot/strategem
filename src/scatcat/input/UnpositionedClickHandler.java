package scatcat.input;

import scatcat.general.points.NormalizedPoint2D;

public interface UnpositionedClickHandler {
    /**
     * Handles the click event.
     * 
     * @param clickLocation Location in screen coordinates where the click occurred.
     * @param myPosition The current position of this object.
     * @return Whether or not the click event was handled.
     */
    boolean handleClick(final NormalizedPoint2D clickLocation,
                        final NormalizedPoint2D myPosition);
}
