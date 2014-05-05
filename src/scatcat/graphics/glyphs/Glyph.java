package scatcat.graphics.glyphs;

import static com.google.common.base.Preconditions.checkArgument;

import android.opengl.GLES20;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

import scatcat.general.points.Point2D;
import scatcat.graphics.DrawUtils;
import scatcat.graphics.MVP;
import scatcat.graphics.RenderableMVP;
import scatcat.graphics.shaders.SimpleTexturedShader;

public class Glyph implements RenderableMVP {
    /** Character this glyph contains. */
    private char character;
    
    /** Which glyph map image the glyph resides in. */
    private final int glyphMapTexture;
    
    /** Size of the glyph map. */
    private static final Point2D glyphMapSize = new Point2D(512.0f, 256.0f);
    
    private final SimpleTexturedShader shader;
    
    /** Packed vertices of the glyph. */
    private int vbo;
    
    /** Width of glyph relative to the unit width. */
    private float width;
    
    /** Parses the text definition of the glyph. */
    public static Glyph parse(final Injector injector,
                              final String[] lines) {
        checkArgument(lines.length == 2, "Expected 2 lines, got %s", lines.length); 
        Glyph glyph = injector.getInstance(Glyph.class);
        
        // Parse the glyph's character
        String[] tokens = lines[0].split(" ");
        glyph.character = tokens[0].charAt(0);
        Log.d("Glyph", "Loading character '" + glyph.character + "'...");
        
        // Parse the texture coordinates
        tokens = lines[0].split(" ");
        final float lowX = Float.parseFloat(tokens[3]);
        final float highX = Float.parseFloat(tokens[4]);
        tokens = lines[1].split(" ");
        final float lowY = Float.parseFloat(tokens[3]);
        final float highY = Float.parseFloat(tokens[4]);
        final float[] vertices = new float[20];
        
        final float unitWidth = 47.0f;
        glyph.width = (highX - lowX) / unitWidth;
        
        // Pack the bottom-left vertex
        vertices[0] = -0.5f; // Position - X
        vertices[1] = -0.5f; // Position - Y
        vertices[2] = 0.0f; // Position - Z
        vertices[3] = lowX / glyphMapSize.getX(); // Texture Coordinate - X
        vertices[4] = highY / glyphMapSize.getY(); // Texture Coordinate - Y
        
        // Pack the top-left vertex
        vertices[5] = -0.5f; // Position - X
        vertices[6] = 0.5f; // Position - Y
        vertices[7] = 0.0f; // Position - Z
        vertices[8] = lowX / glyphMapSize.getX(); // Texture Coordinate - X
        vertices[9] = lowY / glyphMapSize.getY(); // Texture Coordinate - Y
        
        // Pack the the bottom-right vertex
        vertices[10] = 0.5f; // Position - X
        vertices[11] = -0.5f; // Position - Y
        vertices[12] = 0.0f; // Position - Z
        vertices[13] = highX / glyphMapSize.getX(); // Texture Coordinate - X
        vertices[14] = highY / glyphMapSize.getY(); // Texture Coordinate - Y
        
        // Pack the top-right vertex
        vertices[15] = 0.5f; // Position - X
        vertices[16] = 0.5f; // Position - Y
        vertices[17] = 0.0f; // Position - Z
        vertices[18] = highX / glyphMapSize.getX(); // Texture Coordinate - X
        vertices[19] = lowY / glyphMapSize.getY(); // Texture Coordinate - Y
        
        glyph.vbo = DrawUtils.packVerticesIntoVbo(vertices);
        
        return glyph;
    }
    
    @Inject
    protected Glyph(@Named("GlyphMapTexture") int glyphMapTexture,
                    final SimpleTexturedShader shader) {
        this.glyphMapTexture = glyphMapTexture;
        this.shader = shader;
    }
    
    /** {@inheritDoc} */
    @Override
    public void render(MVP mvp) {
        shader.activate();
        
        shader.setMVPMatrix(mvp.collapse());
        shader.setTexture(glyphMapTexture);
        shader.setVBO(vbo);
        shader.draw(GLES20.GL_TRIANGLE_STRIP, 4);
    }
    
    public char getCharacter() { return character; }
    public int getVBO() { return vbo; }
    public float getWidth() { return width; }
    
    public void setCharacter(final char character) { this.character = character; }
    public void setVBO(final int vbo) { this.vbo = vbo; }
}
