package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.util.Collector;

/*package-protected*/ class EnigmaPlayerCollector extends Collector<IEnigmaPlayerListener> implements IEnigmaPlayerListener {
    @Override
    public void _dont_implement_IEnigmaPlayerListener___instead_extend_BaseEnigmaPlayerListener_() {
        //In this case we do want to implement the interface so that we get useful compile time errors.
    }

    @Override
    public void onPlaybackError(Error error) {
        forEach(listener -> listener.onPlaybackError(error));
    }
}