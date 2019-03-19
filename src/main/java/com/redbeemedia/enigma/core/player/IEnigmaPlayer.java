package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.util.IHandler;


public interface IEnigmaPlayer {
    void play(IPlayRequest playerRequest);
    boolean addListener(IEnigmaPlayerListener playerListener);
    boolean removeListener(IEnigmaPlayerListener playerListener);
    IEnigmaPlayer setCallbackHandler(IHandler handler);
    IEnigmaPlayer setCallbackHandler(Handler handler);
}
