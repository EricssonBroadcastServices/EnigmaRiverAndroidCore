package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.player.track.IPlayerImplementationTrack;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.video.IVideoTrack;

import java.util.Collection;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IPlayerImplementationListener {
    void onError(EnigmaError error);
    void onLoadCompleted();
    void onPlaybackStarted();
    void onTimelineBoundsChanged(ITimelinePosition start, ITimelinePosition end);
    void onPositionChanged();
    void onStreamEnded();
    void onPlaybackStopped() ;
    void onPlaybackPaused();
    void onPlaybackBuffering();
    void onTracksChanged(Collection<? extends  IPlayerImplementationTrack> tracks);
    void onAudioTrackSelectionChanged(IAudioTrack track);
    void onSubtitleTrackSelectionChanged(ISubtitleTrack track);
    void onVideoTrackSelectionChanged(IVideoTrack track);

    /**
     * Callback for manifest changes or updated. This gives listeners
     * the option to access raw playback information.
     * The implementation is protocol dependent so call frequency might vary.
     * @param manifestUrl The detected URL to the manifest containing playback metadata.
     * @param streamFormat The <code>StreamFormat</code> being detected for the current manifest.
     * @param startTime An absolute timestamp for the fetched manifest.
     */
    void onManifestChanged(String manifestUrl, EnigmaMediaFormat.StreamFormat streamFormat, long startTime);
}
