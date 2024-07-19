// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.http;

public interface IHttpTask {
    boolean isDone();
    void cancel(long joinMillis);
}
