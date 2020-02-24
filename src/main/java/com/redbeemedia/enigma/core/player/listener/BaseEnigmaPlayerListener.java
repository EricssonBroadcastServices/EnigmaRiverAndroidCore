package com.redbeemedia.enigma.core.player.listener;

import androidx.annotation.NonNull;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.EnigmaPlayerState;

public class BaseEnigmaPlayerListener implements IEnigmaPlayerListener {
    @Override
    @Deprecated
    public final void _dont_implement_IEnigmaPlayerListener___instead_extend_BaseEnigmaPlayerListener_() {
    }

    @Override
    public void onPlaybackError(@NonNull EnigmaError error) {
    }

    @Override
    public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
    }

    @Override
    public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
    }

    @Override
    public void onProgramChanged(IProgram from, IProgram to) {
    }
}
