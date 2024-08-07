// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.util.IStateMachine;
import com.redbeemedia.enigma.core.util.IStateMachineBuilder;
import com.redbeemedia.enigma.core.util.StateMachineBuilder;

/*package-protected*/ class EnigmaStateMachineFactory {
    public static IStateMachine<EnigmaPlayerState> create() {
        IStateMachineBuilder<EnigmaPlayerState> builder = new StateMachineBuilder<>();

        for(EnigmaPlayerState state : EnigmaPlayerState.values()) {
            builder.addState(state);
        }

        builder.setInitialState(EnigmaPlayerState.IDLE);

        builder.addDirectTransition(EnigmaPlayerState.IDLE, EnigmaPlayerState.LOADING);

        builder.addDirectTransition(EnigmaPlayerState.LOADING, EnigmaPlayerState.LOADED);
        builder.addDirectTransition(EnigmaPlayerState.LOADING, EnigmaPlayerState.IDLE);

        builder.addDirectTransition(EnigmaPlayerState.LOADED, EnigmaPlayerState.PLAYING);
        builder.addDirectTransition(EnigmaPlayerState.LOADED, EnigmaPlayerState.IDLE);

        builder.addDirectTransition(EnigmaPlayerState.PLAYING, EnigmaPlayerState.LOADED);
        builder.addDirectTransition(EnigmaPlayerState.PLAYING, EnigmaPlayerState.PAUSED);
        builder.addDirectTransition(EnigmaPlayerState.PLAYING, EnigmaPlayerState.BUFFERING);
        builder.addDirectTransition(EnigmaPlayerState.PLAYING, EnigmaPlayerState.IDLE);

        builder.addDirectTransition(EnigmaPlayerState.PAUSED, EnigmaPlayerState.PLAYING);
        builder.addDirectTransition(EnigmaPlayerState.PAUSED, EnigmaPlayerState.IDLE);
        builder.addDirectTransition(EnigmaPlayerState.PAUSED, EnigmaPlayerState.LOADED);

        builder.addDirectTransition(EnigmaPlayerState.BUFFERING, EnigmaPlayerState.PLAYING);
        builder.addDirectTransition(EnigmaPlayerState.BUFFERING, EnigmaPlayerState.PAUSED);
        builder.addDirectTransition(EnigmaPlayerState.BUFFERING, EnigmaPlayerState.LOADED);
        builder.addDirectTransition(EnigmaPlayerState.BUFFERING, EnigmaPlayerState.IDLE);

        return builder.build();
    }
}
