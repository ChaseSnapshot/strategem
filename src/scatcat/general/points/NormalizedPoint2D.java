package scatcat.general.points;

import android.view.MotionEvent;

public class NormalizedPoint2D extends Point2D {
    public NormalizedPoint2D() {}
    
    public NormalizedPoint2D(final MotionEvent event) {
        //setX(event.getX() / Device.getWidth() - 0.5f);
        //setY(-event.getY() / Device.getHeight() + 0.5f);
    }
    
    public NormalizedPoint2D(float x, float y) {
        setX(x);
        setY(y);
    }
    
    public NormalizedPoint2D add(final NormalizedPoint2D addee) {
        NormalizedPoint2D point = new NormalizedPoint2D();
        
        point.setX(getX() + addee.getX());
        point.setY(getY() + addee.getY());
        
        return point;
    }
    
    @Override
    public void setX(float x) {
        //checkArgument((-0.5f <= x) && (x <= 0.5f), "Expected -0.5f <= x <= 0.5f, got x = %s", x);
        super.setX(x);
    }
    
    @Override
    public void setY(float y) {
        //checkArgument((-0.5f <= y) && (y <= 0.5f), "Expected -0.5f <= y <= 0.5f, got y = %s", y);
        super.setY(y);
    }
}
