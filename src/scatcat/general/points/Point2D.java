package scatcat.general.points;

import android.view.MotionEvent;

public class Point2D {
    private float x = 0.0f;
    private float y = 0.0f;
    
    public Point2D() { }
    
    public Point2D(MotionEvent event) {
        x = event.getX();
        y = event.getY();
    }
    
    public Point2D(final Point2D source) {
        this.x = source.x;
        this.y = source.y;
    }
    
    public Point2D(final float x, final float y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public boolean equals(Object comparee) {
        if (!(comparee instanceof Point2D)) {
            return false;
        }
        
        Point2D otherPoint = (Point2D) comparee;
        return ((this.x == otherPoint.x) && (this.y == otherPoint.y));
    }
    
    public void addToX(float xAdd) { x += xAdd; }
    public void addToY(float yAdd) { y += yAdd; }
    
    public void normalize(float max) {
        x /= max;
        y /= max;
    }
    
    public Point2D subtract(final Point2D subtractee) {
        Point2D result = new Point2D();
        
        result.x = this.x - subtractee.x;
        result.y = this.y - subtractee.y;
        
        return result;
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    
    public void setX(float x) { this.x = x; }
    public void setY(float y) { this.y = y; }
}
