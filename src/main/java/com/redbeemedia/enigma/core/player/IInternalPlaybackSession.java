package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.ads.IAdMetadata;
import com.redbeemedia.enigma.core.analytics.IAnalyticsReporter;
import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.drm.IDrmInfo;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.track.IPlayerImplementationTrack;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;
import com.redbeemedia.enigma.core.video.IVideoTrack;

import java.util.Collection;

/*package-protected*/ interface IInternalPlaybackSession extends IPlaybackSession {
    void onStart(IEnigmaPlayer enigmaPlayer);
    void onStop(IEnigmaPlayer enigmaPlayer);

    IStreamInfo getStreamInfo();
    IAnalyticsReporter getAnalyticsReporter();
    IStreamPrograms getStreamPrograms();
    IPlaybackSessionInfo getPlaybackSessionInfo();
    IDrmInfo getDrmInfo();
    /** Returns ad related information (or `null` if no ad information was detected). */
    IAdMetadata getAdsMetadata();
    IEnigmaPlayerConnection getPlayerConnection();
    void setPlayingFromLive(boolean live);
    void setSeekLiveAllowed(boolean allowed);
    void setTracks(Collection<? extends IPlayerImplementationTrack> tracks);
    void setSelectedSubtitleTrack(ISubtitleTrack track);
    void setSelectedSubtitleTrack(String trackId);
    ISubtitleTrack getSubtitleTrack(String trackId);
    void setSelectedAudioTrack(IAudioTrack track);
    void setSelectedAudioTrack(String trackId);
    IAudioTrack getAudioTrack(String trackId);
    void setSelectedVideoTrack(IVideoTrack track);
    void setSelectedVideoTrack(String trackId);

    void fireEndReached();
    void fireSeekCompleted();

    void addInternalListener(IInternalPlaybackSessionListener listener);
}
