package com.redbeemedia.enigma.core.player.listener;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.player.EnigmaPlayerState;

public class BaseEnigmaPlayerListener implements IEnigmaPlayerListener {
    @Override
    @Deprecated
    public final void _dont_implement_IEnigmaPlayerListener___instead_extend_BaseEnigmaPlayerListener_() {
    }

    @Override
    public void onPlaybackError(Error error) {
    }

    @Override
    public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
    }
}
