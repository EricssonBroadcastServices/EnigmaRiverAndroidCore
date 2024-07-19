// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.listener.IEnigmaPlayerListener;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.util.Collector;

/*package-protected*/ class EnigmaPlayerCollector extends Collector<IEnigmaPlayerListener> implements IEnigmaPlayerListener {

    public EnigmaPlayerCollector() {
        super(IEnigmaPlayerListener.class);
    }

    @Override
    public void _dont_implement_IEnigmaPlayerListener___instead_extend_BaseEnigmaPlayerListener_() {
        //In this case we do want to implement the interface so that we get useful compile time errors.
    }

    @Override
    public void onPlaybackError(EnigmaError error) {
        forEach(listener -> listener.onPlaybackError(error));
    }

    @Override
    public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
        forEach(listener -> listener.onStateChanged(from, to));
    }

    @Override
    public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
        forEach(listener -> listener.onPlaybackSessionChanged(from, to));
    }

    @Override
    public void onProgramChanged(IProgram from, IProgram to) {
        forEach(listener -> listener.onProgramChanged(from, to));
    }

    @Override
    public void checkEntitlement(IProgram to) {
        forEach(listener -> listener.checkEntitlement(to));
    }

    @Override
    public void sendPlaybackStartedEvent() {
        forEach(IEnigmaPlayerListener::sendPlaybackStartedEvent);
    }

    @Override
    public void setPlayerImplementationControls(IPlayerImplementationControls playerImplementationControls) {

    }
}
