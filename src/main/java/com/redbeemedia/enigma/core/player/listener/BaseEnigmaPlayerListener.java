package com.redbeemedia.enigma.core.player.listener;

import com.redbeemedia.enigma.core.error.Error;

public abstract class BaseEnigmaPlayerListener implements IEnigmaPlayerListener {
    @Override
    @Deprecated
    public void _dont_implement_IEnigmaPlayerListener___instead_extend_BaseEnigmaPlayerListener_() {
    }

    @Override
    public void onPlaybackError(Error error) {
    }
}
