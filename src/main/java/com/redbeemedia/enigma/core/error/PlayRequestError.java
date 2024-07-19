// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;

import com.redbeemedia.enigma.core.playrequest.IPlayRequest;
import com.redbeemedia.enigma.core.player.IEnigmaPlayer;


/**
 * Subclasses of this error is used when there was a problem carrying
 * out the PlayRequest sent in
 * {@link IEnigmaPlayer#play(IPlayRequest)}.
 */
public abstract class PlayRequestError extends EnigmaError {
    /*package-protected*/ PlayRequestError() {
        this(null, null);
    }

    /*package-protected*/ PlayRequestError(EnigmaError cause) {
        this(null, cause);
    }

    /*package-protected*/ PlayRequestError(String message) {
        this(message, null);
    }

    /*package-protected*/ PlayRequestError(String message, EnigmaError cause) {
        super(message, cause);
    }

}
