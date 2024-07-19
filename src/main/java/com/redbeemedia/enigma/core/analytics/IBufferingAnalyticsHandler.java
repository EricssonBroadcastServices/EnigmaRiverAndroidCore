// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.analytics;

public interface IBufferingAnalyticsHandler extends IAnalyticsHandler {
    void sendData() throws AnalyticsException, InterruptedException;
}
