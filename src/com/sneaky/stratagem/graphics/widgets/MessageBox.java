package com.sneaky.stratagem.graphics.widgets;

import android.opengl.Matrix;

import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.assistedinject.Assisted;

import scatcat.general.Constants;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.glyphs.GlyphParagraph;
import scatcat.graphics.glyphs.GlyphString;
import scatcat.graphics.glyphs.GlyphString.GlyphStringFactory;
import scatcat.graphics.shaders.SimpleTexturedShader;

public class MessageBox implements RenderableMVP {
    /** Width to height ratio of the message box's size. Used to avoid warped text. */
    private final float aspectRatio;
    
    private final int backgroundTexture;
    
    /** Body text displayed when rendering the message box. */
    private Optional<GlyphParagraph> bodyText = Optional.<GlyphParagraph>absent();
    private float bodyTextHeight = 0.6f;
    private final float bodyTextBottomMargin = 0.05f;
    private final float bodyTextTopMargin = 0.05f;
    private final float bodyTextWidth = 0.9f;
    
    /** Heading text displayed when rendering the message box. */
    private Optional<GlyphString> headerText = Optional.<GlyphString>absent();
    private float headerTextHeight = 0.3f;
    private final float headerTextTopMargin = 0.05f;

    private final Injector injector;
    
    /** Used to render the message box. */
    private final SimpleTexturedShader shader;
    
    @Inject
    protected MessageBox(@Assisted("aspectRatio") final float aspectRatio,
                         @Assisted("backgroundTexture") final int backgroundTexture,
                         final Injector injector,
                         final SimpleTexturedShader shader) {
        this.aspectRatio = aspectRatio;
        this.backgroundTexture = backgroundTexture;
        this.injector = injector;
        this.shader = shader;
    }
    
    public interface MessageBoxFactory {
        MessageBox create(@Assisted("aspectRatio") final float aspectRatio,
                          @Assisted("backgroundTexture") final int backgroundTexture);
    }
    
    @Override
    public void render(MVP mvp) {
        checkState(headerText.isPresent(), "Header text has not been set. Have you called setHeaderText()?");
        
        shader.activate();
        
        // Render the background texture
        float[] model = mvp.peekCopy(MVP.Type.MODEL);
        shader.setMVPMatrix(mvp.collapseM(model));
        shader.setTexture(backgroundTexture);
        shader.draw();
        
        // Undo the affect of the message boxes sizing on the text
        //Matrix.scaleM(model, Constants.NO_OFFSET, 1.0f / aspectRatio, aspectRatio, 1.0f);
        
        // Render the header
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, 
                0.5f - headerTextTopMargin - headerTextHeight / 2.0f, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        Matrix.scaleM(model, Constants.NO_OFFSET, 1.0f / aspectRatio, 1.0f, 1.0f);
        mvp.push(MVP.Type.MODEL, model);
        headerText.get().render(mvp);
        mvp.pop(MVP.Type.MODEL);
        model = mvp.pop(MVP.Type.MODEL);
        
        // Render the body
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f,
                -headerTextHeight / 2.0f - bodyTextTopMargin - bodyTextHeight / 2.0f, 0.0f);
        mvp.push(MVP.Type.MODEL, model);
        bodyText.get().render(mvp);
        model = mvp.pop(MVP.Type.MODEL);
    }

    public void setBodyText(final String bodyText, final float lineHeight) {
        GlyphParagraph text = injector.getInstance(GlyphParagraph.class);
        text.setHeight(bodyTextHeight);
        text.setLineHeight(lineHeight);
        text.setWidth(bodyTextWidth);
        text.setAspectRatio(aspectRatio);
        text.setParagraph(bodyText);
        
        this.bodyText = Optional.<GlyphParagraph>of(text);
    }
    
    public void setHeaderText(final String headerText, final float lineHeight) {
        GlyphStringFactory glyphStringFactory = injector.getInstance(GlyphStringFactory.class);
        
        this.headerTextHeight = lineHeight;
        this.bodyTextHeight = 1.0f - headerTextHeight - headerTextTopMargin - bodyTextTopMargin - bodyTextBottomMargin;
        
        this.headerText = Optional.<GlyphString>of(glyphStringFactory.create(headerText, headerTextHeight));
    }
}
