/*
 * SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
 *
 * SPDX-License-Identifier: MIT
 */
package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.analytics.IAnalyticsReporter;
import com.redbeemedia.enigma.core.analytics.SilentAnalyticsReporter;

class Analytics {
    private final IAnalyticsReporter analyticsReporter;
    private final IInternalPlaybackSessionListener internalPlaybackSessionListener;

    Analytics(IAnalyticsReporter analyticsReporter, IInternalPlaybackSessionListener internalPlaybackSessionListener) {
        this.analyticsReporter = analyticsReporter;
        this.internalPlaybackSessionListener = internalPlaybackSessionListener;
    }

    public IAnalyticsReporter getAnalyticsReporter() {
        return analyticsReporter;
    }

    public IInternalPlaybackSessionListener getInternalPlaybackSessionListener() {
        return internalPlaybackSessionListener;
    }

    /**
     * Returns an analytics reporter that does not send any data.
     */
    static Analytics silentAnalitycs() {
        return new Analytics(
                new SilentAnalyticsReporter(), new IInternalPlaybackSessionListener() {
            public void onStart(OnStartArgs args) { }
            public void onStop(OnStopArgs args) {}
        });
    }
}
