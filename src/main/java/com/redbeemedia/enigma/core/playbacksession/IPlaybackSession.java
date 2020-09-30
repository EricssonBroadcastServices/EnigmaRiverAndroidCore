package com.redbeemedia.enigma.core.playbacksession;

import android.os.Handler;

import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.playable.IPlayable;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.video.IVideoTrack;

import java.util.List;

public interface IPlaybackSession {
    void addListener(IPlaybackSessionListener listener);
    void addListener(IPlaybackSessionListener listener, Handler handler);
    void removeListener(IPlaybackSessionListener listener);
    IPlayable getPlayable();
    boolean isPlayingFromLive();
    boolean isSeekToLiveAllowed();
    boolean isSeekAllowed();

    /**
     * Garuaneed to not be <code>null</code>.
     * @return Contract restrictions for the PlaybackSession
     */
    IContractRestrictions getContractRestrictions();
    /**
     * Used to override contract restrictions. Typically for debugging.
     *
     * @param contractRestrictions Must not be null.
     * @throws java.lang.NullPointerException if <code>contractRestrictions</code> is <code>null</code>.
     */
    void setContractRestrictions(IContractRestrictions contractRestrictions);
    /**
     * Returns the latest list supplied in the {@link com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener#onSubtitleTracks(List)}-event or <code>null</code> if no such event has been fired yet.
     *
     * @return Current list of subtitle tracks or <code>null</code>.
     */
    List<ISubtitleTrack> getSubtitleTracks();
    ISubtitleTrack getSelectedSubtitleTrack();
    /**
     * Returns the latest list supplied in the {@link com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener#onSubtitleTracks(List)}-event or <code>null</code> if no such event has been fired yet.
     *
     * @return Current list of audio tracks or <code>null</code>.
     */
    List<IAudioTrack> getAudioTracks();
    IAudioTrack getSelectedAudioTrack();

    IVideoTrack getSelectedVideoTrack();
}
