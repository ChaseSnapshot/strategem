package com.sneaky.stratagem.armyselection;

import android.opengl.Matrix;
import scatcat.general.Constants;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.shaders.SimpleTexturedShader;

/** Base entry in the Army Selection interface. */
public abstract class Entry implements RenderableMVP {
    /** Background rectangle properties. */
    private final int backgroundTexture;
    private float height;
    private float width;

    /** Shader program for rendering. */
    private final SimpleTexturedShader shader;
    
    /** Injection construction is not needed as this entry is abstract/non-composed. */
    protected Entry(final int backgroundTexture,
                    final float height,
                    final SimpleTexturedShader shader,
                    final float width) {
        this.backgroundTexture = backgroundTexture;
        this.height = height;
        this.shader = shader;
        this.width = width;
    }
    
    public void render(final MVP mvp) {
        shader.activate();
        
        // Render the background rectangle
        final float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.scaleM(model, Constants.NO_OFFSET, width, height, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(backgroundTexture);
        shader.draw();
    }
    
    public final float getHeight() { return height; }
    public final float getWidth() { return width; }
}
