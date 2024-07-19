// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.util.IStateMachine;

import org.junit.Test;

public class EnigmaStateMachineFactoryTest {
    @Test
    public void testAllEnigmaPlayerStatesReachable() {
        IStateMachine<EnigmaPlayerState> stateMachine = EnigmaStateMachineFactory.create();

        for(EnigmaPlayerState state1 : EnigmaPlayerState.values()) {
            for(EnigmaPlayerState state2 : EnigmaPlayerState.values()) {
                if(state1 == state2) {
                    continue;
                }
                stateMachine.setState(state1);
                stateMachine.setState(state2);
            }
        }
    }
}
