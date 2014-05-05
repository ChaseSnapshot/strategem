package scatcat.input;

import scatcat.general.points.Point2D;

public class InputHelper {
    
    public static boolean isTouched(final Point2D position, final float width, final float height,
            final Point2D touchLocation) {
        return ((position.getX() - width / 2.0f <= touchLocation.getX()) &&
                (position.getX() + width / 2.0f >= touchLocation.getX()) && 
                (position.getY() - height / 2.0f <= touchLocation.getY()) &&
                (position.getY() + height / 2.0f >= touchLocation.getY()));
    }
}
