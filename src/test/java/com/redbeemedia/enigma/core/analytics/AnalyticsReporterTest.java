// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.analytics;

import android.util.Log;
import android.util.MockLog;

import com.redbeemedia.enigma.core.error.ErrorCode;
import com.redbeemedia.enigma.core.error.IllegalSeekPositionError;
import com.redbeemedia.enigma.core.error.PlayerImplementationError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.MockTimeProvider;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintStream;

public class AnalyticsReporterTest {
    private PrintStream originalOutput;

    @Before
    public void disableLogging() {
        this.originalOutput = MockLog.getOut();
        MockLog.setOut(null);
    }

    @After
    public void restoreLogging() {
        MockLog.setOut(originalOutput);
    }

    @Test
    public void testOnErrorWithSimpleError() {
        final Counter onAnalyticsCalled = new Counter();
        AnalyticsReporter analyticsReporter = new AnalyticsReporter(new MockTimeProvider(1000), new IAnalyticsHandler() {
            @Override
            public void onAnalytics(JSONObject jsonObject) {
                try {
                    Assert.assertEquals("Playback.Error", jsonObject.getString("EventType"));
                    Assert.assertEquals(ErrorCode.UNEXPECTED, jsonObject.getInt("Code"));
                    Assert.assertEquals(1000, jsonObject.getLong("Timestamp"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                onAnalyticsCalled.count();
            }
        },0l);
        onAnalyticsCalled.assertNone();
        analyticsReporter.playbackError(new UnexpectedError("UnitTest"));
        onAnalyticsCalled.assertOnce();
    }

    @Test
    public void testOnErrorWithPlayerImplementationError() {
        final Counter onAnalyticsCalled = new Counter();
        AnalyticsReporter analyticsReporter = new AnalyticsReporter(new MockTimeProvider(1000), new IAnalyticsHandler() {
            @Override
            public void onAnalytics(JSONObject jsonObject) {
                try {
                    Assert.assertEquals("Playback.Error", jsonObject.getString("EventType"));
                    Assert.assertEquals(ErrorCode.PLAYER_IMPLEMENTATION_ERROR, jsonObject.getInt("Code"));
                    Assert.assertEquals(1000, jsonObject.getLong("Timestamp"));
                    Assert.assertEquals(777, jsonObject.getInt("UnitTestPlayerErrorCode"));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                onAnalyticsCalled.count();
            }
        },0l);
        onAnalyticsCalled.assertNone();
        analyticsReporter.playbackError(new PlayerImplementationError(777, "UnitTestPlayerErrorCode"));
        onAnalyticsCalled.assertOnce();
    }

    @Test
    public void testNoEventsAfterTerminalAborted() {
        final Counter onAnalyticsCalled = new Counter();
        AnalyticsReporter analyticsReporter = new AnalyticsReporter(new MockTimeProvider(), new IAnalyticsHandler() {
            @Override
            public void onAnalytics(JSONObject jsonObject) {
                onAnalyticsCalled.count();
            }
        },0l);
        onAnalyticsCalled.assertCount(0);

        analyticsReporter.playbackResumed(Duration.hours(1).inWholeUnits(Duration.Unit.MILLISECONDS));
        onAnalyticsCalled.assertCount(1);

        analyticsReporter.playbackAborted(Duration.minutes(2).inWholeUnits(Duration.Unit.MILLISECONDS));
        onAnalyticsCalled.assertCount(2);

        analyticsReporter.playbackPaused(Duration.minutes(5).inWholeUnits(Duration.Unit.MILLISECONDS));
        onAnalyticsCalled.assertCount(2);

    }

    @Test
    public void testNoEventsAfterTerminalError() {
        final Counter onAnalyticsCalled = new Counter();
        AnalyticsReporter analyticsReporter = new AnalyticsReporter(new MockTimeProvider(), new IAnalyticsHandler() {
            @Override
            public void onAnalytics(JSONObject jsonObject) {
                onAnalyticsCalled.count();
            }
        },0l);
        onAnalyticsCalled.assertCount(0);

        analyticsReporter.playbackHeartbeat(Duration.hours(1).inWholeUnits(Duration.Unit.MILLISECONDS));
        onAnalyticsCalled.assertCount(1);

        analyticsReporter.playbackError(new IllegalSeekPositionError());
        onAnalyticsCalled.assertCount(2);

        analyticsReporter.playbackHandshakeStarted("mockAsset");
        onAnalyticsCalled.assertCount(2);
    }

    @Test
    public void testNoEventsAfterTerminalCompleted() {
        final Counter onAnalyticsCalled = new Counter();
        AnalyticsReporter analyticsReporter = new AnalyticsReporter(new MockTimeProvider(), new IAnalyticsHandler() {
            @Override
            public void onAnalytics(JSONObject jsonObject) {
                onAnalyticsCalled.count();
            }
        },0l);
        onAnalyticsCalled.assertCount(0);

        analyticsReporter.playbackCreated("mockAsset");
        onAnalyticsCalled.assertCount(1);

        analyticsReporter.playbackCompleted(Duration.seconds(123).inWholeUnits(Duration.Unit.MILLISECONDS));
        onAnalyticsCalled.assertCount(2);

        analyticsReporter.playbackPlayerReady(Duration.hours(100).inWholeUnits(Duration.Unit.MILLISECONDS), "MockTech", "1.0");
        onAnalyticsCalled.assertCount(2);
    }

}
