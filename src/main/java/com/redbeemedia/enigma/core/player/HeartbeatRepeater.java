/*
 * SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
 *
 * SPDX-License-Identifier: MIT
 */
package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.analytics.IAnalyticsReporter;
import com.redbeemedia.enigma.core.task.ITaskFactoryProvider;
import com.redbeemedia.enigma.core.task.Repeater;
import com.redbeemedia.enigma.core.util.function.ISupplier;

public class HeartbeatRepeater extends Repeater {
    final IAnalyticsReporter analyticsReporter;
    final ISupplier<Long> getCurrentOffset;
    public HeartbeatRepeater(
            ITaskFactoryProvider taskFactoryProvider,
            IAnalyticsReporter analyticsReporter,
            ISupplier<Long> getCurrentOffset
    ) {
        super(
                taskFactoryProvider.getMainThreadTaskFactory(),
                HEARTBEAT_RATE_MILLIS,
                new HeartbeatRunnable(analyticsReporter, getCurrentOffset)
        );

        this.analyticsReporter = analyticsReporter;
        this.getCurrentOffset = getCurrentOffset;
    }

    static class HeartbeatRunnable implements Runnable {
        final IAnalyticsReporter analyticsReporter;
        final ISupplier<Long> getCurrentOffset;
        private Long lastOffset = null;
        HeartbeatRunnable(IAnalyticsReporter analyticsReporter,
                          ISupplier<Long> getCurrentOffset
        ) {
            this.analyticsReporter = analyticsReporter;
            this.getCurrentOffset = getCurrentOffset;
        }

        @Override
        public void run() {
            try {
                lastOffset = getCurrentOffset.get();
            } catch (Exception e) {
                //Ignore
            }
            if(lastOffset != null) {
                analyticsReporter.playbackHeartbeat(lastOffset);
            }
        }
    }

    private static final long HEARTBEAT_RATE_MILLIS = 60L * 1000L;
}
