package com.redbeemedia.enigma.core.analytics;

import com.redbeemedia.enigma.core.error.ErrorCode;
import com.redbeemedia.enigma.core.error.PlayerImplementationError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.time.MockTimeProvider;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class AnalyticsReporterTest {
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
        });
        onAnalyticsCalled.assertNone();
        analyticsReporter.error(new UnexpectedError("UnitTest"));
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
        });
        onAnalyticsCalled.assertNone();
        analyticsReporter.error(new PlayerImplementationError(777, "UnitTestPlayerErrorCode"));
        onAnalyticsCalled.assertOnce();
    }
}
