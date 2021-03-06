package com.redbeemedia.enigma.core.player;

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
    IStreamPrograms getStreamPrograms();
    IPlaybackSessionInfo getPlaybackSessionInfo();
    IDrmInfo getDrmInfo();
    IEnigmaPlayerConnection getPlayerConnection();
    void setPlayingFromLive(boolean live);
    void setTracks(Collection<? extends IPlayerImplementationTrack> tracks);
    void setSelectedSubtitleTrack(ISubtitleTrack track);
    void setSelectedAudioTrack(IAudioTrack track);
    void setSelectedVideoTrack(IVideoTrack track);

    void fireEndReached();
    void fireSeekCompleted();

    void addInternalListener(IInternalPlaybackSessionListener listener);
}
