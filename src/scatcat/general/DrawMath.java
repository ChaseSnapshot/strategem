package scatcat.general;

import scatcat.general.points.CartesianScreenPoint2D;
import android.util.FloatMath;

/**
 * Utility class containing a variety of mathematical functions.
 * 
 * @author R. Matt McCann
 */
public final class DrawMath {
    /** Hidden constructor. */
    private DrawMath() { }
    
    /**
     * Calculate the distance between the position and the touch event.
     * 
     * @param mTouchStart The touch location.
     * @param position The position to calculate distance to.
     * @return The distance.
     */
    public static float calcDistance(Position mTouchStart, Position position) {
        float xFactor = mTouchStart.getX() - position.getX();
        float yFactor = mTouchStart.getY() - position.getY();
        
        return FloatMath.sqrt(xFactor * xFactor + yFactor * yFactor);
    }
    
    public static float calcDistance(CartesianScreenPoint2D start, CartesianScreenPoint2D stop) {
        float xFactor = start.getX() - stop.getX();
        float yFactor = start.getY() - stop.getY();
        
        return FloatMath.sqrt(xFactor * xFactor + yFactor * yFactor);
    }

    /**
     * Finds the smallest power of two larger than x.
     * 
     * @param x Value to find the power of two ceiling for.
     * @return Power of two ceiling.
     */
    public static float findCeilingPowerOfTwo(final float x) {
        float ceiling = 2;
        
        while (ceiling <= x) {
            ceiling *= 2;
        }
        
        return ceiling;
    }    
}
