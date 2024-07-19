// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util;

public interface IHandler {
    boolean post(Runnable runnable);
    boolean postDelayed(Runnable runnable, long delayMillis);
}
