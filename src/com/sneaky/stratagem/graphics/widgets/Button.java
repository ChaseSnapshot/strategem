package com.sneaky.stratagem.graphics.widgets;

import android.opengl.Matrix;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import scatcat.general.Constants;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.glyphs.GlyphString;
import scatcat.graphics.glyphs.GlyphString.GlyphStringFactory;
import scatcat.graphics.shaders.SimpleTexturedShader;

public class Button implements RenderableMVP {
    private final int backgroundTexture;
    
    /** Button dimensions. */
    private final float height;
    private final float width;
    
    /** Used to render the button. */
    private final SimpleTexturedShader shader;
    
    /** Text displayed when rendering the button. */
    private final GlyphString text;
    
    /** Guice injection constructor. */
    @Inject
    protected Button(final SimpleTexturedShader shader,
                     @Assisted("backgroundTexture") final int backgroundTexture,
                     final GlyphStringFactory glyphStringFactory,
                     @Assisted("height") final float height,
                     @Assisted("text") final String text,
                     @Assisted("textHeightRatio") final float textHeightRatio,
                     @Assisted("width") final float width) {
        this.backgroundTexture = backgroundTexture;
        this.height = height;
        this.shader = shader;
        this.text = glyphStringFactory.create(text, height * textHeightRatio);
        this.width = width;
    }
    public interface ButtonFactory {
        Button create(@Assisted("backgroundTexture") int backgroundTexture, 
                      @Assisted("height") float height, @Assisted("text") String text, 
                      @Assisted("textHeightRatio") float textHeightRatio, 
                      @Assisted("width") float width);
    }
    
    public final float getHeight() { return height; }
    
    public final GlyphString getText() { return text; }
    
    public final float getWidth() { return width; }
    
    @Override
    public void render(MVP mvp) {
        shader.activate();
        
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        Matrix.scaleM(model, Constants.NO_OFFSET, width, height, 1.0f);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(backgroundTexture);
        shader.draw();
        
        model = mvp.peekCopy(MVP.Type.MODEL);
        if (text.getWidth() > width * 0.9f) {
            Matrix.scaleM(model, Constants.NO_OFFSET, (width * 0.9f) / text.getWidth(), 1.0f, 1.0f);
        }
        mvp.push(MVP.Type.MODEL, model);
        text.render(mvp);
        mvp.pop(MVP.Type.MODEL);
    }
}
