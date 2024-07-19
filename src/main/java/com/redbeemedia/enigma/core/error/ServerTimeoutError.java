// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;



public class ServerTimeoutError extends ServerError {
    public ServerTimeoutError() {
        this(null, null);
    }

    public ServerTimeoutError(EnigmaError cause) {
        this(null, cause);
    }

    public ServerTimeoutError(String message) {
        this(message, null);
    }

    public ServerTimeoutError(String message, EnigmaError cause) {
        super(message, cause);
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.SERVER_TIMEOUT;
    }
}
