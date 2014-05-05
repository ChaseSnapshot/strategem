package com.sneaky.stratagem.units.commanders;

import com.google.inject.Injector;
import com.sneaky.stratagem.units.Unit;

import scatcat.graphics.MVP;

public abstract class Commander extends Unit {
    /** Guice injection constructor. */
    protected Commander(final Injector injector,
                        final int profileTexture,
                        final int facingNorthTexture,
                        final int facingEastTexture,
                        final int facingSouthTexture,
                        final int facingWestTexture) {
        super(injector, profileTexture, facingNorthTexture, facingEastTexture,
                facingSouthTexture, facingWestTexture);
    }
    
    @Override
    public void render(final MVP mvp) {
        super.render(mvp);
    }
}
