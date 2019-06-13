package com.redbeemedia.enigma.core.playbacksession;

import android.os.Handler;

import com.redbeemedia.enigma.core.playable.IPlayable;

public interface IPlaybackSession {
    void addListener(IPlaybackSessionListener listener);
    void addListener(IPlaybackSessionListener listener, Handler handler);
    void removeListener(IPlaybackSessionListener listener);
    IPlayable getPlayable();
    boolean isPlayingFromLive();
    boolean isSeekToLiveAllowed();
    boolean isSeekAllowed();
}
