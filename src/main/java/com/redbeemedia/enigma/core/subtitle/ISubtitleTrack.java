package com.redbeemedia.enigma.core.subtitle;

import com.redbeemedia.enigma.core.track.ITrack;

/**
 * <h3>NOTE</h3>
 * <p>Implementing or extending this interface is not part of the public API.</p>
 */
public interface ISubtitleTrack extends ITrack {
    String getLabel();
    String getCode();
}
