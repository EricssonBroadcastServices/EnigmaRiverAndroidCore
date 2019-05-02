package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MockInternalPlaybackSession implements IInternalPlaybackSession {
    private StreamInfo streamInfo;

    public MockInternalPlaybackSession(boolean live) {
        this(live, -1L);
    }

    public MockInternalPlaybackSession(boolean live, long start) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("live", live);
            jsonObject.put("start", start);
            this.streamInfo = new StreamInfo(jsonObject);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onStart(IEnigmaPlayer enigmaPlayer) {
    }

    @Override
    public void onStop(IEnigmaPlayer enigmaPlayer) {
    }

    @Override
    public StreamInfo getStreamInfo() {
        return streamInfo;
    }

    @Override
    public void setPlayingFromLive(boolean live) {
    }

    @Override
    public void fireEndReached() {
    }

    @Override
    public void addListener(IPlaybackSessionListener listener) {
    }

    @Override
    public void addListener(IPlaybackSessionListener listener, Handler handler) {
    }

    @Override
    public void removeListener(IPlaybackSessionListener listener) {
    }

    @Override
    public boolean isPlayingFromLive() {
        return false;
    }

    @Override
    public boolean isSeekAllowed() {
        return true;
    }

    @Override
    public boolean isSeekToLiveAllowed() {
        return true;
    }
}
