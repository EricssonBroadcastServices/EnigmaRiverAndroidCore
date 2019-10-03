package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.Error;

/**
 * Serves as a way for the current playback session to communicate back to EnigmaPlayer
 */
/*package-protected*/ interface IEnigmaPlayerConnection {
    void openConnection(ICommunicationsChannel communicationsChannel);
    void severConnection();

    interface ICommunicationsChannel {
        void onPlaybackError(Error error, boolean endStream);
    }
}
