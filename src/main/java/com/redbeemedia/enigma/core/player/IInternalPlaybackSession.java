package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;

/*package-protected*/ interface IInternalPlaybackSession extends IPlaybackSession {
    void onStart(IEnigmaPlayer enigmaPlayer);
    void onStop(IEnigmaPlayer enigmaPlayer);

    StreamInfo getStreamInfo();
    void setPlayingFromLive(boolean live);
}
