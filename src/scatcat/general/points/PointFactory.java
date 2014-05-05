package scatcat.general.points;

import android.view.MotionEvent;

import com.google.inject.Inject;

import scatcat.general.Device;

public class PointFactory {
    private final Device device;
    
    @Inject
    public PointFactory(final Device device) {
        this.device = device;
    }
    
    public CartesianScreenPoint2D createCartesianScreenPoint2D(final MotionEvent event) {
        CartesianScreenPoint2D point = new CartesianScreenPoint2D();
        
        point.setX(event.getX() - device.getWidth() / 2.0f);
        point.setY(-event.getY() + device.getHeight() / 2.0f);
        
        return point;
    }
    
    public NormalizedPoint2D createNormalizedPoint2D(final MotionEvent event) {
        NormalizedPoint2D point = new NormalizedPoint2D();
        
        point.setX(event.getX() / device.getWidth() - 0.5f);
        point.setY(-event.getY() / device.getHeight() + 0.5f);
        
        return point;
    }
    
    public NormalizedPoint2D createNormalizedPoint2D(final CartesianScreenPoint2D inPoint) {
        NormalizedPoint2D outPoint = new NormalizedPoint2D();
        
        outPoint.setX(inPoint.getX() / device.getWidth());
        outPoint.setY(inPoint.getY() / device.getHeight());
        
        return outPoint;
    }
}
