package scatcat.input;

import scatcat.general.points.NormalizedPoint2D;

public interface LongPressHandler {
    /**
     * Handles the long press event.
     * 
     * @param pressLocation Location in screen coordinates where the long press occurred.
     * @return Whether or not the long press event was handled.
     */
    boolean handleLongPress(final NormalizedPoint2D pressLocation);
}