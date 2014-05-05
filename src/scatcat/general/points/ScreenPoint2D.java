package scatcat.general.points;

import scatcat.general.Device;
import android.view.MotionEvent;

/**
 * Wrapper class to improve code readability.
 * 
 * @author R. Matt McCann
 */
public class ScreenPoint2D extends Point2D {
    /**
     * Constructor.
     * 
     * @param event Screen event to extract its point location from.
     */
    public ScreenPoint2D(final MotionEvent event) {
        super(event);
    }
    
    public ScreenPoint2D(final NormalizedPoint2D toBeConverted) {
        //setX(toBeConverted.getX() + Device.getWidth() / 2.0f);
        //setY(-(toBeConverted.getY() - Device.getHeight() / 2.0f));
    }
    
    /**
     * Constructor.
     * 
     * @param toBeCopied Screen point to be copied.
     */
    public ScreenPoint2D(final ScreenPoint2D toBeCopied) {
        setX(toBeCopied.getX());
        setY(toBeCopied.getY());
    }

    public ScreenPoint2D() { }

    public ScreenPoint2D(float newX, float newY) {
        setX(newX);
        setY(newY);
    }
}
