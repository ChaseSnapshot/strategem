package scatcat.general.points;

import static com.google.common.base.Preconditions.checkArgument;
import android.view.MotionEvent;
import scatcat.general.Device;

/**
 * Screen point subscribing to a Cartesian plane with its origin in the center of the screen.
 * X values must range from [-Device.width/2 , Device.width/2]
 * Y values must range from [-Device.height/2, Device.height/2]
 * 
 * @author R. Matt McCann
 */
public class CartesianScreenPoint2D extends ScreenPoint2D {
    public CartesianScreenPoint2D() {
        setX(0.0f);
        setY(0.0f);
    }
    
    public CartesianScreenPoint2D(final MotionEvent event) {
        /*final float inputX = event.getX() - Device.getWidth() / 2.0f;
        final float inputY = -event.getY() + Device.getHeight() / 2.0f;
        
        checkArgument(Math.abs(inputX) <= Device.getWidth() / 2.0f,
                "X = " + inputX + ": Magnitude must be <= " + Device.getWidth() / 2.0f);
        checkArgument(Math.abs(inputY) <= Device.getHeight() / 2.0f,
                "Y = " + inputX + ": Magnitude must be <= " + Device.getHeight() / 2.0f);
        
        setX(inputX);
        setY(inputY);*/
    }
    
    /**
     * Constructs a Cartesian screen point from an existing non-Cartesian screen point.
     * 
     * @param toBeConverted Non-Cartesian point to be converted.
     */
    public CartesianScreenPoint2D(final ScreenPoint2D toBeConverted) {
        /*final float inputX = toBeConverted.getX() - Device.getWidth() / 2.0f;
        final float inputY = -toBeConverted.getY() + Device.getHeight() / 2.0f;
        
        checkArgument(Math.abs(inputX) <= Device.getWidth() / 2.0f,
                "X = " + inputX + ": Magnitude must be <= " + Device.getWidth() / 2.0f);
        checkArgument(Math.abs(inputX) <= Device.getHeight() / 2.0f,
                "Y = " + inputX + ": Magnitude must be <= " + Device.getHeight() / 2.0f);
        
        setX(inputX);
        setY(inputY);*/
    }
    
    /** Copies an existing Cartesian screen point. */
    public CartesianScreenPoint2D(final CartesianScreenPoint2D toBeCopied) {
        setX(toBeCopied.getX());
        setY(toBeCopied.getY());
    }
  
    public CartesianScreenPoint2D(final float x, final float y) {
        super(x, y);
    }

    public CartesianScreenPoint2D add(final CartesianScreenPoint2D addee) {
        CartesianScreenPoint2D result = new CartesianScreenPoint2D();
        
        result.setX(this.getX() + addee.getX());
        result.setY(this.getY() + addee.getY());
        
        return result;
    }
    
    public CartesianScreenPoint2D subtract(final CartesianScreenPoint2D subtractee) {
        CartesianScreenPoint2D result = new CartesianScreenPoint2D();
        
        result.setX(getX() - subtractee.getX());
        result.setY(getY() - subtractee.getY());
        
        return result;
    }

    @Override
    public String toString() {
        return "(" + getX() + "," + getY() + ")";
    }
}
