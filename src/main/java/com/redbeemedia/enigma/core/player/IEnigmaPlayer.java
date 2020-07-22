package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.IHandler;


public interface IEnigmaPlayer {
    void play(IPlayRequest playRequest);
    boolean addListener(IEnigmaPlayerListener playerListener);
    boolean addListener(IEnigmaPlayerListener playerListener, Handler handler);
    boolean removeListener(IEnigmaPlayerListener playerListener);
    IEnigmaPlayerControls getControls();
    ITimeline getTimeline();
    EnigmaPlayerState getState();
    IEnigmaPlayer setCallbackHandler(IHandler handler);
    IEnigmaPlayer setCallbackHandler(Handler handler);
    void setDefaultSession(ISession session);
    void release();
}
