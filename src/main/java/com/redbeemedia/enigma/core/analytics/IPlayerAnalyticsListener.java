/*
 * SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
 *
 * SPDX-License-Identifier: MIT
 */
package com.redbeemedia.enigma.core.analytics;

public interface IPlayerAnalyticsListener {
    void attach(
            AnalyticsReporter reporter,
            Runnable onPlaybackFinishedEvent,
            Runnable onPlaybackDestroyedEvent,
            AnalyticsPlayResponseData analyticsPlayResponseData,
            String playMode,
            String assetId
    );

    void detach();
    long getCurrentTimeInSeconds();
    String getTechnology();
}
