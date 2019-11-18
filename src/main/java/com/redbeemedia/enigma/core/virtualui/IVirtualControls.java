package com.redbeemedia.enigma.core.virtualui;

/**
 * <h3>NOTE</h3>
 * <p>Implementing or extending this interface is not part of the public API.</p>
 */
public interface IVirtualControls {
    IVirtualButton getRewind();
    IVirtualButton getFastForward();
    IVirtualButton getPlay();
    IVirtualButton getPause();
    IVirtualButton getGoToLive();
    IVirtualButton getNextProgram();
    IVirtualButton getRestart();
}
