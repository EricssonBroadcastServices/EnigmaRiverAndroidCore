package com.redbeemedia.enigma.core.virtualui;

import androidx.annotation.NonNull;

/**
 * <h3>NOTE</h3>
 * <p>Implementing or extending this interface is not part of the public API.</p>
 */
public interface IVirtualControls {
    @NonNull IVirtualButton getRewind();
    @NonNull IVirtualButton getFastForward();
    @NonNull IVirtualButton getPlay();
    @NonNull IVirtualButton getPause();
    @NonNull IVirtualButton getGoToLive();
    @NonNull IVirtualButton getNextProgram();
    @NonNull IVirtualButton getPreviousProgram();
    @NonNull IVirtualButton getRestart();
}
