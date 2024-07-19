// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;

public class MigratedUserError extends CredentialsError {
    public static final String ERROR = "MigratedUserError";

    public MigratedUserError() {
        this(null, null);
    }

    public MigratedUserError(EnigmaError cause) {
        this(null, cause);
    }

    public MigratedUserError(String message) {
        this(message, null);
    }

    public MigratedUserError(String message, EnigmaError cause) {
        super(message, cause);
    }

    @Override
    public int getErrorCode() {
        return ErrorCode.MIGRATED_USER;
    }
}
