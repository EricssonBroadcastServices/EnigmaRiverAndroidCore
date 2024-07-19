// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

public interface IPlayerImplementation {
    void install(IEnigmaPlayerEnvironment environment);
    void release();
    void updateTimeBar(long millis);
}
