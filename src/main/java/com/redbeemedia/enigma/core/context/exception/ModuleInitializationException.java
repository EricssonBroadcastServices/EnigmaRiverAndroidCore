// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.context.exception;

public class ModuleInitializationException extends Exception {
    public ModuleInitializationException() {
    }

    public ModuleInitializationException(String message) {
        super(message);
    }

    public ModuleInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModuleInitializationException(Throwable cause) {
        super(cause);
    }
}
