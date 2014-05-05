package scatcat.general.points;

public class Point3D extends Point2D {
    private float z = 0.0f;
    
    public Point3D() { }
    
    public Point3D(float x, float y, float z) {
        super(x, y);
        
        this.z = z;
    }

    public final void addToZ(final float zAdd) {
        this.z += zAdd;
    }
    
    @Override
    public Point3D clone() {
        Point3D clone = new Point3D();
        
        clone.setX(this.getX());
        clone.setY(this.getY());
        clone.setZ(this.getZ());
        
        return clone;
    }
    
    public final float getZ() { return z; }
    
    public void normalize(float max) {
        super.normalize(max);
        z /= max;
    }
    
    public final void setZ(float z) { this.z = z; }
}
