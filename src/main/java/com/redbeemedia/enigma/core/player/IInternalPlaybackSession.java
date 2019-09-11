package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.audio.IAudioTrack;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.track.IPlayerImplementationTrack;
import com.redbeemedia.enigma.core.subtitle.ISubtitleTrack;

import java.util.Collection;

/*package-protected*/ interface IInternalPlaybackSession extends IPlaybackSession {
    void onStart(IEnigmaPlayer enigmaPlayer);
    void onStop(IEnigmaPlayer enigmaPlayer);

    StreamInfo getStreamInfo();
    IStreamPrograms getStreamPrograms();
    void setPlayingFromLive(boolean live);
    void fireEndReached();
    void setTracks(Collection<? extends IPlayerImplementationTrack> tracks);
    void setSelectedSubtitleTrack(ISubtitleTrack track);
    void setSelectedAudioTrack(IAudioTrack track);
}
