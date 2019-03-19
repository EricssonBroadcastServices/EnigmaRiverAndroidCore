package com.redbeemedia.enigma.core.analytics;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class JsonAnalyticsEventBuilderTest {

    @Test
    public void testJsonConstruction() throws JSONException {
        JsonAnalyticsEventBuilder<AnalyticsEvents.AnalyticsErrorEvent> builder = new JsonAnalyticsEventBuilder<>("TestType", 12345L);
        builder.addData(AnalyticsEvents.ERROR.CODE, 123);
        builder.addData(AnalyticsEvents.ERROR.MESSAGE, "Test message");
        JSONObject jsonObject = builder.build();

        Assert.assertEquals("TestType", jsonObject.get("EventType"));
        Assert.assertEquals(12345L, jsonObject.get("Timestamp"));
        Assert.assertEquals(123, jsonObject.get("Code"));
        Assert.assertEquals("Test message", jsonObject.get("Message"));
    }
}
