package com.sneaky.stratagem.obstacles;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.graphics.MVP;
import scatcat.graphics.shaders.SimpleTexturedShader;
import android.opengl.Matrix;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.sneaky.stratagem.units.Unit;

public class MasonsWall extends Unit {
    private final Injector injector;
    
    private final int texture;
    
    @Inject
    protected MasonsWall(Injector injector, 
                         SimpleTexturedShader shader,
                         @Named("MasonsWall") final int texture) {
        super(injector, texture, texture, texture, texture, texture);
        
        this.injector = injector;
        this.texture = texture;
        
        setName("Wall");
        setChargePoints(0);
        setHealth(7);
        setMaxChargePoints(0);
        setMaxHealth(7);
    }
    
    @Override
    public void render(final MVP mvp) {
        Device device = injector.getInstance(Device.class);
        SimpleTexturedShader shader = injector.getInstance(SimpleTexturedShader.class);
        
        shader.activate();
        
        // Draw the wall
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.scaleM(model, Constants.NO_OFFSET, 1.0f, device.getAspectRatio(), 1.0f);
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, 0.45f, 0.05f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(texture);
        shader.draw();
        
        renderEffects(mvp);
    }
}
