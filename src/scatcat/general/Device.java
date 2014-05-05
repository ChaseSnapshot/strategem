package scatcat.general;

import com.google.inject.Inject;

import android.app.Activity;
import android.graphics.Point;

/**
 * Singleton encapsulating all of the necessary device-specific
 * details such as pixel dimensions of the screen, etc.
 * 
 * @author R. Matt McCann
 */
public final class Device {
    private final float height;
    private final float width;
    
    @Inject
    public Device(final Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        
        height = size.y;
        width = size.x;
    }
    
    public final float getAspectRatio() { return width / height; }
    public final float getHeight() { return height; }
    public final float getWidth() { return width; }
}
