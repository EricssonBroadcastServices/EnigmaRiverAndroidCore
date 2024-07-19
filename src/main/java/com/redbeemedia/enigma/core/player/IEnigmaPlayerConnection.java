// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.EnigmaError;

/**
 * Serves as a way for the current playback session to communicate back to EnigmaPlayer
 */
/*package-protected*/ interface IEnigmaPlayerConnection {
    void openConnection(ICommunicationsChannel communicationsChannel);
    void severConnection();

    interface ICommunicationsChannel {
        void onPlaybackError(EnigmaError error, boolean endStream);
        void onExpirePlaybackSession(PlaybackSessionSeed playbackSessionSeed);
    }
}
