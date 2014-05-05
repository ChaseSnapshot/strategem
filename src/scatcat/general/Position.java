package scatcat.general;

import android.view.MotionEvent;

/**
 * Encapsulates a 3D point.
 * 
 * @author R. Matt McCann
 */
public class Position {
    /**
     * Constructs position from motion event.
     * 
     * @param event Event to construct from.
     */
    public Position(MotionEvent event) {
        mX = event.getX();
        mY = event.getY();
        mZ = 0.0f;
    }

    /**
     * Simple constructor.
     */
    public Position() { }

    /**
     * Constructor.
     * 
     * @param x X value.
     * @param y Y value.
     * @param z Z value.
     */
    public Position(final float x, final float y, final float z) {
        mX = x;
        mY = y;
        mZ = z;
    }

    /**
     * Converts the motion event into a position value.
     * 
     * @param event The event to take properties from.
     */
    public final void set(MotionEvent event) {
        mX = event.getX();
        mY = event.getY();
        mZ = 0.0f;
    }

    public final float getX() {
        return mX;
    }

    public final float getY() {
        return mY;
    }

    public final float getZ() {
        return mZ;
    }

    /**
     * Adds a scalar to the X component.
     * 
     * @param xAddition Value to add.
     */
    public final void addX(final float xAddition) {
        mX += xAddition;
    }

    /**
     * Adds a scalar to the Y component.
     * 
     * @param yAddition Value to add.
     */
    public final void addY(final float yAddition) {
        mY += yAddition;
    }

    /**
     * Adds a scalar to the Z component.
     * 
     * @param zAddition Value to add.
     */
    public final void addZ(final float zAddition) {
        mZ += zAddition;
    }

    /**
     * Subtract a position.
     * 
     * @param value The position to subtract.
     */
    public final void subtract(final Position value) {
        mX -= value.mX;
        mY -= value.mY;
        mZ -= value.mZ;
    }
    
    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return "(" + mX + "," + mY + "," + mZ + ")";
    }
    
    private float mX;
    private float mY;
    private float mZ;
}
