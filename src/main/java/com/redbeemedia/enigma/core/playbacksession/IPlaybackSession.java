package com.redbeemedia.enigma.core.playbacksession;

import android.os.Handler;

public interface IPlaybackSession {
    void addListener(IPlaybackSessionListener listener);
    void addListener(IPlaybackSessionListener listener, Handler handler);
    void removeListener(IPlaybackSessionListener listener);
    boolean isPlayingFromLive();
    boolean isSeekToLiveAllowed();
    boolean isSeekAllowed();
}
