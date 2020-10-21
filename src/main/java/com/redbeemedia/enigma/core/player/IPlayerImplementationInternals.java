package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IPlayerImplementationInternals {

    /**
     * Returns the current playback position in the current content window (or ad)
     * @return the playback position or null
     */
    ITimelinePosition getCurrentPosition();

    /**
     * @return The start position of the stream or null.
     */
    ITimelinePosition getCurrentStartBound();

    /**
     * @return Returns the end of the stream or null.
     */
    ITimelinePosition getCurrentEndBound();

    /**
     * Returns the position considered to be the "live" position during linear playback.
     * @return a ITimelinePosition if found or null.
     */
    ITimelinePosition getLivePosition();

    IPlaybackTechnologyIdentifier getTechnologyIdentifier();
}
