package com.sneaky.stratagem.armyselection;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.name.Named;

import scatcat.graphics.shaders.SimpleTexturedShader;

public class EmptyEntry extends Entry {
    /** Injection Constructor. */
    @Inject
    protected EmptyEntry(@Named("EmptyEntryBackgroundTexture") final int backgroundTexture,
                         final SimpleTexturedShader shader,
                         @Assisted("height") final float height,
                         @Assisted("width") final float width) {
        super(backgroundTexture, height, shader, width);
    }
    public interface EmptyEntryFactory {
        EmptyEntry create(@Assisted("height") float height, @Assisted("width") float width);
    }
}
