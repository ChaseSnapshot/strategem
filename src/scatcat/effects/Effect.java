package scatcat.effects;

import com.sneaky.stratagem.flow.Updatable;

import scatcat.general.Cleanable;
import scatcat.graphics.RenderableMVP;

public interface Effect extends RenderableMVP, Updatable, Cleanable { 
    Effect clone();
}
