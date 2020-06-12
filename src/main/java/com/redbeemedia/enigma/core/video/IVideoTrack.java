package com.redbeemedia.enigma.core.video;

import com.redbeemedia.enigma.core.track.ITrack;

/**
 * <h3>NOTE</h3>
 * <p>Implementing or extending this interface is not part of the public API.</p>
 */
public interface IVideoTrack extends ITrack {
    /**
     * @return Bitrate in bits per second or -1 if N/A
     */
    int getBitrate();
}
