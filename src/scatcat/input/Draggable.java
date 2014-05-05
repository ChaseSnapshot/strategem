package scatcat.input;

import scatcat.general.points.NormalizedPoint2D;

public interface Draggable {
    boolean handlePickUp(final NormalizedPoint2D touchLocation);
    boolean handleDrag(final NormalizedPoint2D moveVector);
    boolean handleDrop(final NormalizedPoint2D dropLocation);
}
