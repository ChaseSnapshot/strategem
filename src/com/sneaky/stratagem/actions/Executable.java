package com.sneaky.stratagem.actions;

import com.google.common.base.Optional;

import scatcat.general.Cleanable;

/**
 * Performs an action. This interface is executed by click handlers.
 * 
 * @author R. Matt McCann
 */
public abstract class Executable {
    private Optional<Cleanable> caller = Optional.absent();
    
    private Optional<Integer> iconTexture = Optional.absent();
    
    private boolean isExecutable = true;
    
    private String name;
    
    /** Cleans up any changes caused by executing the action. */
    public void cleanUp() { }
    
    @Override
    public abstract Executable clone();
    
    /** Performs an action. */
    public abstract void execute();
    
    public Cleanable getCaller() { return caller.get(); }
    public Integer getIconTexture() { return iconTexture.get(); }
    public String getName() { return name; }
    
    public boolean hasCaller() { return caller.isPresent(); }
    public boolean hasIconTexture() { return iconTexture.isPresent(); }
    
    public boolean isExecutable() { return isExecutable; }
    
    public void setCaller(final Cleanable caller) { this.caller = Optional.of(caller); }
    public void setIconTexture(final int iconTexture) { this.iconTexture = Optional.of(iconTexture); }
    public void setIsExecutable(final boolean isExecutable) { this.isExecutable = isExecutable; }
    public void setName(final String name) { this.name = name; }
}
