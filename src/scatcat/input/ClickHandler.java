package scatcat.input;

import scatcat.general.points.NormalizedPoint2D;

public interface ClickHandler {
    /**
     * Handles the click event.
     * 
     * @param clickLocation Location in screen coordinates where the click occurred.
     * @return Whether or not the click event was handled.
     */
    boolean handleClick(final NormalizedPoint2D clickLocation);
}
