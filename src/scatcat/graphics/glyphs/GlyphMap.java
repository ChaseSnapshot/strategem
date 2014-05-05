package scatcat.graphics.glyphs;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import android.content.Context;
import android.util.Log;

@Singleton
public class GlyphMap {
    private final Map<Character, Glyph> map = new HashMap<Character, Glyph>();
    
    @Inject
    protected GlyphMap(final Context context,
                       @Named("GlyphInfo") final int glyphInfoResourceID,
                       final Injector injector) throws Exception {
        // Open the glyph info file
        final BufferedReader glyphInfo = new BufferedReader(new InputStreamReader(
                context.getResources().openRawResource(glyphInfoResourceID)));
        
        // Read in the number of glyphs defined in the information file
        final int numGlyphs = Integer.parseInt(glyphInfo.readLine().split(" ")[0]);
        Log.d("GlyphMap", "Loading " + numGlyphs + " glyphs...");
        
        // Parse the glyph info to created the glyph map
        for (int glyphIter = 0; glyphIter < numGlyphs; glyphIter++) {
            String[] glyphDefinition = new String[2];
            
            // Read in the definition
            glyphDefinition[0] = glyphInfo.readLine();
            glyphDefinition[1] = glyphInfo.readLine();
            
            // Construct the glyph and add it to the map
            Glyph glyph = Glyph.parse(injector, glyphDefinition);
            map.put(glyph.getCharacter(), glyph);
            
            // Throw away the spacing line
            glyphInfo.readLine();
        }
    }
    
    public Glyph get(final Character key) { 
        checkArgument(map.containsKey(key), "Map does not contain the key '" + key + "'!");
        
        return map.get(key); 
    }
}
