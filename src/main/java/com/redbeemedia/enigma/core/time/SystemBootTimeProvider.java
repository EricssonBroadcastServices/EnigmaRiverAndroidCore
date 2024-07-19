// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.time;

import android.os.SystemClock;

/**
 * A <code>ITimeProvider</code> that provides the milliseconds since system was booted.
 */
public class SystemBootTimeProvider implements ITimeProvider {
    @Override
    public long getTime() {
        return SystemClock.elapsedRealtime();
    }

    @Override
    public boolean isReady(Duration maxBlocktime) {
        return true;
    }
}
