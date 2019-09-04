package com.redbeemedia.enigma.core.playbacksession;

import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.util.IInternalListener;

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
    void onSubtitleTracks(List<ISubtitleTrack> tracks);
    void onSelectedSubtitleTrackChanged(ISubtitleTrack oldselectedTrack, ISubtitleTrack newSelectedTrack);
}
