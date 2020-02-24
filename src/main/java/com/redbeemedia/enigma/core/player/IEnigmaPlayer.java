package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import androidx.annotation.NonNull;

import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.util.IHandler;


public interface IEnigmaPlayer {
    void play(IPlayRequest playerRequest);
    boolean addListener(IEnigmaPlayerListener playerListener);
    boolean addListener(IEnigmaPlayerListener playerListener, Handler handler);
    boolean removeListener(IEnigmaPlayerListener playerListener);
    @NonNull IEnigmaPlayerControls getControls();
    @NonNull ITimeline getTimeline();
    EnigmaPlayerState getState();
    IEnigmaPlayer setCallbackHandler(IHandler handler);
    IEnigmaPlayer setCallbackHandler(Handler handler);
}
