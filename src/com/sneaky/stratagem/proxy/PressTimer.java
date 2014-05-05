package com.sneaky.stratagem.proxy;

import scatcat.general.points.NormalizedPoint2D;

public class PressTimer implements Runnable {
    private boolean isLongPress = false;
    
    private boolean isShuttingDown = false;
    
    private final long longPressTime = 500;
    
    private final NormalizedPoint2D pressLocation;
    
    private final ProxyRenderer proxyRenderer;
    
    private final long startTime;
    
    public PressTimer(final ProxyRenderer proxyRenderer, final long startTime, 
                      final NormalizedPoint2D pressLocation) {
        this.proxyRenderer = proxyRenderer;
        this.startTime = startTime;
        this.pressLocation = pressLocation;
    }
    
    public boolean isLongPress() { return isLongPress; }
    
    @Override
    public void run() {
        while (!isShuttingDown) {
            if (System.currentTimeMillis() - startTime > longPressTime) {
                isLongPress = true;
                proxyRenderer.handleLongPress(pressLocation);
                break;
            }
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) { }
        }
    }
    
    public void setIsShuttingDown(final boolean isShuttingDown) {
        this.isShuttingDown = isShuttingDown;
    }
}