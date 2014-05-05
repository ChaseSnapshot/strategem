package scatcat.graphics.glyphs;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.opengl.Matrix;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import scatcat.general.Constants;
import scatcat.general.Device;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.shaders.SimpleTexturedShader;

@SuppressLint("DefaultLocale")
public class GlyphString implements RenderableMVP {
    private final Device device;
    
    /** Mapping of characters to their corresponding glyph. */
    private final GlyphMap glyphMap;
    
    private HorizontalAlign horizontalAlignment = HorizontalAlign.CENTER;
    
    /** Spacing between the letters in percentage of the unit letter width. */
    private float letterSpacing = 0.05f;
    
    /** String to draw with glyphs. */
    private List< Optional<Glyph> > message;
    
    /** Height of the letters in percent of total height. */
    private float relativeHeight = 1.0f;
    
    /** Width of the letters in percent of original width. */
    private float relativeWidth = 0.5f;
    
    private final SimpleTexturedShader shader;
    
    public enum HorizontalAlign {
        LEFT, CENTER, RIGHT
    }
    public enum VerticalAlign {
        BOTTOM, CENTER, TOP
    }
    
    /** Guice injection constructor. */
    @Inject
    protected GlyphString(final Device device,
                          final GlyphMap glyphMap,
                          final SimpleTexturedShader shader,
                          @Assisted final String text,
                          @Assisted final float height) {
        this.device = device;
        this.glyphMap = glyphMap;
        this.relativeHeight = height;
        this.shader = shader;
        
        setMessage(text);
    }
    public interface GlyphStringFactory {
        GlyphString create(final String text, final float height);
    }
    
    public float getWidth() {
        float width = 0.0f;
        
        for (int iter = 0; iter < message.size(); iter++) {
            Optional<Glyph> glyph = message.get(iter);
            
            if (glyph.isPresent()) {
                width += relativeHeight * relativeWidth * glyph.get().getWidth();
            } else {
                width += relativeHeight * relativeWidth / 2.0f;
            }
            
            if (iter + 1 < message.size()) {
                width += letterSpacing * relativeHeight;
            }
        }
        
        return width;
    }
    
    @Override
    public void render(MVP mvp) {
        float xPosition = 0.0f;
        
        // Calculate the total string width
        float messageWidth = getWidth();
        
        // Calculate the width of the first glyph
        float firstLetterWidth = 0.0f;
        Iterator<Optional<Glyph>> glyphIter = message.iterator();
        if (glyphIter.hasNext()) {
            firstLetterWidth = glyphIter.next().get().getWidth() * relativeWidth * relativeHeight;
        }
        
        // Align the text
        switch (horizontalAlignment) {
            case CENTER:
                xPosition = (-messageWidth + firstLetterWidth) / 2.0f;
            default:
                break;
        }
        
        shader.activate();
    
        for (int iter = 0; iter < message.size(); iter++) {
            Optional<Glyph> glyph = message.get(iter);
            
            if (glyph.isPresent()) {
                float[] model = mvp.peekCopy(MVP.Type.MODEL);

                Matrix.translateM(model, Constants.NO_OFFSET, xPosition, 0.0f, 0.0f);
                Matrix.scaleM(model, Constants.NO_OFFSET, relativeHeight, relativeHeight, 1.0f);
                Matrix.scaleM(model, Constants.NO_OFFSET, relativeWidth, 1.0f, 1.0f);
                Matrix.scaleM(model, Constants.NO_OFFSET, glyph.get().getWidth(), 1.0f, 1.0f);
            
                mvp.push(MVP.Type.MODEL, model);
                glyph.get().render(mvp);
                mvp.pop(MVP.Type.MODEL);
            }         
            
            
            if (iter +  1 < message.size()) {
                if (glyph.isPresent()) {
                    xPosition += glyph.get().getWidth() * relativeHeight * relativeWidth / 2.0f;
                } else {
                    xPosition += relativeHeight * relativeWidth / 2.0f;
                }
                xPosition += letterSpacing * relativeHeight;
                
                if (message.get(iter + 1).isPresent()) {
                    xPosition += message.get(iter + 1).get().getWidth() * relativeHeight * relativeWidth / 2.0f;
                }
            }
        }
    }
    
    public void setHorizontalAlignment(final HorizontalAlign horizontalAlignment) {
        this.horizontalAlignment = checkNotNull(horizontalAlignment);
    }
    public void setLetterSpacing(final float letterSpacing) {
        checkArgument(0.0f <= letterSpacing, 
                "Letter spacing must be at least 0.0 or greater, got %s", letterSpacing);
        this.letterSpacing = letterSpacing;
    }
    public void setMessage(final String message) {
        this.message = new ArrayList< Optional<Glyph> >();
        
        for (int charIter = 0; charIter < message.length(); charIter++) {
            char current = message.toLowerCase().charAt(charIter);
            
            if (current == ' ') {
                this.message.add(Optional.<Glyph>absent());
            } else {
                Glyph glyph = glyphMap.get(current);
                this.message.add(Optional.<Glyph>of(glyph));
            }
        }
    }
    public void setRelativeHeight(final float relativeHeight) {
        checkArgument(0.0f <= relativeHeight, "Relative height must be greater than 0, got %s", relativeHeight);
        this.relativeHeight = relativeHeight;
    }
    public void setRelativeWidth(final float relativeWidth) {
        checkArgument((0.0f <= relativeWidth) && (relativeWidth <= 1.0f),
                "Relative width must be between 0 and 1, got %s", relativeWidth);
        this.relativeWidth = relativeWidth * device.getHeight() / device.getWidth();
    }
}
