// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util.error;

import com.redbeemedia.enigma.core.error.EnigmaError;

public class EnigmaErrorException extends RuntimeException{
    private final EnigmaError error;

    public EnigmaErrorException(EnigmaError error) {
        super(error.getClass().getSimpleName()+" ("+error.getErrorCode()+")");
        this.error = error;
    }

    public EnigmaError getError() {
        return error;
    }
}
