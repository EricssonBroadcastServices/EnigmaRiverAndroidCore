package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.player.track.IPlayerImplementationTrack;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;

import java.util.Collection;

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
    void onTracksChanged(Collection<? extends  IPlayerImplementationTrack> tracks);
    void onAudioTrackSelectionChanged(IAudioTrack track);
    void onSubtitleTrackSelectionChanged(ISubtitleTrack track);
}
