package com.redbeemedia.enigma.core.virtualui;

import androidx.annotation.NonNull;

import com.redbeemedia.enigma.core.time.Duration;

/**
 * <h3>NOTE</h3>
 * <p>Implementing or extending this interface is not part of the public API.</p>
 * <p>Instead extends {@link VirtualControlsSettings} to ensure future compatibility.</p>
 */
public interface IVirtualControlsSettings {
    @NonNull Duration getSeekForwardStep();
    @NonNull Duration getSeekBackwardStep();
}
