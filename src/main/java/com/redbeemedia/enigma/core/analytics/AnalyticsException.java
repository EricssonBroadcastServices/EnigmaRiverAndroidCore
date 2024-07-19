// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.analytics;

import java.io.IOException;

/**
 * Signals that there was a problem when sending or constructing analytics data.
 */
public class AnalyticsException extends IOException {
    public AnalyticsException() {
    }

    public AnalyticsException(String message) {
        super(message);
    }

    public AnalyticsException(String message, Throwable cause) {
        super(message, cause);
    }

    public AnalyticsException(Throwable cause) {
        super(cause);
    }
}
