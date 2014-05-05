package com.sneaky.stratagem.map.background;

import java.util.Comparator;
import java.util.Random;

import com.google.inject.Inject;
import com.sneaky.stratagem.flow.Updatable;

import scatcat.general.Constants;
import scatcat.general.points.Point2D;
import scatcat.general.points.Point3D;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.shaders.SimpleTexturedShader;
import android.opengl.Matrix;

public class Cloud implements RenderableMVP, Updatable {
    /** Speed at which the cloud floats. */
    private float floatSpeed;
    
    /** Current position of the cloud. */
    private Point3D position = new Point3D();
    
    private final SimpleTexturedShader shader;
    
    /** Size of the cloud. */
    private Point2D size = new Point2D();
    
    /** Cloud texture. */
    private int texture;
	
	public final Point3D getPosition() { return position; }

	@Inject
	protected Cloud(final SimpleTexturedShader shader) {
	    this.shader = shader;
	}
	
	/** {@inheritDoc} */
    @Override
    public void render(MVP mvp) {
        shader.activate();
        
        // Position the cloud
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.translateM(model, Constants.NO_OFFSET, position.getX(), position.getY(), position.getZ());
        Matrix.scaleM(model, Constants.NO_OFFSET, size.getX(), size.getY(), 1.0f);
        
        // Draw the cloud
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(texture);
        shader.draw();
    }
    
    public final void setFloatSpeed(final float floatSpeed) { this.floatSpeed = floatSpeed; }
    public final void setPosition(final Point3D position) { this.position = position; }
    public final void setSize(final Point2D size) { this.size = size; }
    public final void setTexture(final int texture) { this.texture = texture; }

    @Override
    public void updateState(int updatesPerSecond) {
        position.addToX(floatSpeed / updatesPerSecond);
        
        if (position.getX() >= 1.0f) {
            Random randomizer = new Random();
            position.setX(-1.0f);
            position.setY(0.5f - randomizer.nextFloat());
        }
    }
    
    public static class CloudComparer implements Comparator<Cloud> {
        @Override
        public int compare(Cloud cloud1, Cloud cloud2) {
            Point3D cloud1Pos = cloud1.getPosition();
            Point3D cloud2Pos = cloud2.getPosition();
            
            if (cloud1Pos.getZ() < cloud2Pos.getZ()) {
                return -1;
            } else if (cloud1Pos.getZ() == cloud2Pos.getZ()) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
