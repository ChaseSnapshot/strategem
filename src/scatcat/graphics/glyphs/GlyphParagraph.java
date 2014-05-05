package scatcat.graphics.glyphs;

import java.util.ArrayList;
import java.util.List;

import android.opengl.Matrix;

import com.google.inject.Inject;

import scatcat.general.Constants;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.glyphs.GlyphString.GlyphStringFactory;

public class GlyphParagraph implements RenderableMVP {
    private float aspectRatio = 1.0f;
    
    private final GlyphStringFactory glyphStringFactory;
    
    private float height = 1.0f;
    
    private float letterWidth = 1.0f;
    
    private float lineHeight = 0.1f;
    
    private float lineSpacing = 0.05f;
    
    private final List<List<GlyphString>> paragraph = new ArrayList<List<GlyphString>>();
    
    private final List<Float> totalLineSpacing = new ArrayList<Float>();
    
    private float width = 1.0f;
    
    @Inject
    protected GlyphParagraph(final GlyphStringFactory glyphStringFactory) {
        this.glyphStringFactory = glyphStringFactory;
    }
    
    @Override
    public void render(MVP mvp) {
        float[] model = mvp.peekCopy(MVP.Type.MODEL);

        // Move to the top of the paragraph
        float topOfParagraph = lineHeight * paragraph.size() + lineSpacing * (paragraph.size() - 1);
        topOfParagraph *= height * 0.5f;
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, topOfParagraph, 0.0f);
        
        // Move down half the height of the first line
        Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, -height * lineHeight / 2.0f, 0.0f);
        
        // For each line
        for (int lineIter = 0; lineIter < paragraph.size(); lineIter++) {
            // Move to the left edge of the paragraph
            Matrix.translateM(model, Constants.NO_OFFSET, -0.5f * width, 0.0f, 0.0f);
            
            // For each word in the line
            List<GlyphString> currentLine = paragraph.get(lineIter);
            float wordSpacing = totalLineSpacing.get(lineIter);
            if (currentLine.size() > 1) { 
                wordSpacing /= (currentLine.size() - 1); 
            }
            for (int wordIter = 0; wordIter < currentLine.size(); wordIter++) {
                final GlyphString currentWord = currentLine.get(wordIter);
                final float currentWordWidth = currentWord.getWidth() / aspectRatio;
                
                // Move to the center of the word's position
                Matrix.translateM(model, Constants.NO_OFFSET, currentWordWidth / 2.0f, 0.0f, 0.0f);
                
                // Render the current word
                mvp.push(MVP.Type.MODEL, model);
                Matrix.scaleM(model, Constants.NO_OFFSET, 1.0f / aspectRatio, 1.0f, 1.0f);
                mvp.push(MVP.Type.MODEL, model);
                currentWord.render(mvp);
                mvp.pop(MVP.Type.MODEL);
                model = mvp.pop(MVP.Type.MODEL);
                
                // Move to the right edge of the word
                Matrix.translateM(model, Constants.NO_OFFSET, currentWordWidth / 2.0f, 0.0f, 0.0f);
                
                // Move an even split of the word spacing
                Matrix.translateM(model, Constants.NO_OFFSET, wordSpacing, 0.0f, 0.0f);
            }
            
            // Move down the line spacing
            Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, -lineSpacing * height, 0.0f);
            
            // Move to the center of the line
            Matrix.translateM(model, Constants.NO_OFFSET, 0.0f, -lineHeight * height / 2.0f, 0.0f);
        }
    }
    
    public void setAspectRatio(final float aspectRatio) { this.aspectRatio = aspectRatio; }
    
    public void setHeight(final float height) { this.height = height; }
    
    public void setLineHeight(final float lineHeight) {
        this.lineHeight = lineHeight;
    }
    
    public void setLineSpacing(final float lineSpacing) {
        this.lineSpacing = lineSpacing;
    }
    
    public void setParagraph(final String paragraph) {
        List<GlyphString> words = new ArrayList<GlyphString>();
        
        final String[] tokens = paragraph.split(" ");
        for (String token : tokens) {
            GlyphString word = glyphStringFactory.create(token, lineHeight * height);
            word.setRelativeWidth(letterWidth);
            
            words.add(word);
        }
        
        int wordIter = 0;
        while(wordIter < words.size()) {
            List<GlyphString> line = new ArrayList<GlyphString>();
            float currentWidth = 0.0f;
            while ((currentWidth < width) && (wordIter < words.size())) {
                GlyphString currentWord = words.get(wordIter);
                final float currentWordWidth = currentWord.getWidth() / aspectRatio;
                
                if (currentWidth + currentWordWidth < width) {
                    line.add(currentWord);
                    currentWidth += currentWordWidth;
                    wordIter++;
                } else {
                    break;
                }
            }
            
            this.paragraph.add(line);
            totalLineSpacing.add(width - currentWidth);
            
            if (wordIter < words.size()) { break; }
        }
    }
    
    public void setLetterWidth(final float letterWidth) {
        this.letterWidth = letterWidth;
    }
    
    public void setWidth(final float width) { this.width = width; }
}
