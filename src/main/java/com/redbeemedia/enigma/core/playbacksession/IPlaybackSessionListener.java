package com.redbeemedia.enigma.core.playbacksession;

import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.util.IInternalListener;
import com.redbeemedia.enigma.core.video.IVideoTrack;

import java.util.List;

public interface IPlaybackSessionListener extends IInternalListener {
    /**
     * <p>Inspired by {@code org.hamcrest.Matcher} from JUnit lib.</p>
     * <br>
     * <p style="margin-left: 25px; font-weight:bold;">It's easy to ignore JavaDoc, but a bit harder to ignore compile errors .</p>
     * <p style="margin-left: 50px">-- Hamcrest source</p>
     */
    @Deprecated
    void _dont_implement_IPlaybackSessionListener___instead_extend_BasePlaybackSessionListener_();

    void onPlayingFromLiveChanged(boolean live);
    void onEndReached();

    /**
     * <p>This event is triggered by the specific
     * {@link com.redbeemedia.enigma.core.player.IPlayerImplementation} and may be fired at any
     * time.</p>
     * <p>Listeners should update their list of {@code tracks} and associated UI any time this event
     * is fired.</p>
     *
     * @param tracks the current list of available {@link ISubtitleTrack}s. Never @code null}, but might be empty.
     */
    void onSubtitleTracks(List<ISubtitleTrack> tracks);
    void onSelectedSubtitleTrackChanged(ISubtitleTrack oldSelectedTrack, ISubtitleTrack newSelectedTrack);

    /**
     * <p>This event is triggered by the specific
     * {@link com.redbeemedia.enigma.core.player.IPlayerImplementation} and may be fired at any
     * time.</p>
     * <p>Listeners should update their list of {@code tracks} and associated UI any time this event
     * is fired.</p>
     *
     * @param tracks the current list of available {@link IAudioTrack}s. Never {@code null}, but might be empty.
     */
    void onAudioTracks(List<IAudioTrack> tracks);
    void onSelectedAudioTrackChanged(IAudioTrack oldSelectedTrack, IAudioTrack newSelectedTrack);

    void onSelectedVideoTrackChanged(IVideoTrack oldSelectedTrack, IVideoTrack newSelectedTrack);

    void onContractRestrictionsChanged(IContractRestrictions oldContractRestrictions, IContractRestrictions newContractRestrictions);
}
