package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IPlayerImplementationListener {
    void onError(Error error);
    void onLoadCompleted();
    void onPlaybackStarted();
    void onTimelineBoundsChanged(ITimelinePosition start, ITimelinePosition end);
    void onPositionChanged();
    void onStreamEnded();
    void onPlaybackPaused();
}
