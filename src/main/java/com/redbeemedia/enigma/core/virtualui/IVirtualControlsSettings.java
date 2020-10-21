package com.redbeemedia.enigma.core.virtualui;

import com.redbeemedia.enigma.core.time.Duration;

/**
 * <h3>NOTE</h3>
 * <p>Implementing or extending this interface is not part of the public API.</p>
 * <p>Instead extends {@link VirtualControlsSettings} to ensure future compatibility.</p>
 */
public interface IVirtualControlsSettings {

    Duration getSeekForwardStep();
    Duration getSeekBackwardStep();

    /**
     * Is a short time span that is intended to be used relative
     * to an absolute live position. If a position is within
     * the time span provided by such "live position" and getLivePositionVicinityThreshold(),
     * it's considered to be "in vicinity" of the live position.
     * @return a short Duration
     */
    Duration getLivePositionVicinityThreshold();
}
