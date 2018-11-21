package com.redbeemedia.enigma.core.error;

import com.redbeemedia.enigma.core.json.JsonInputStreamParser;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

public class ExposureHttpErrorTest {

    @Test
    public void testGetHttpError() throws JSONException {

        JsonInputStreamParser jsonInputStreamParser = JsonInputStreamParser.obtain();

        ByteArrayInputStream bais = new ByteArrayInputStream("{\"message\": \"UNKNOWN_BUSINESS_UNIT. If the business unit cannot be found.\", \"httpCode\": 404}".getBytes(Charset.forName("utf-8")));
        JSONObject jsonObject = jsonInputStreamParser.parse(bais);

        JSONObject expectedJsonObject = new JSONObject();
        expectedJsonObject.put("message", "UNKNOWN_BUSINESS_UNIT. If the business unit cannot be found.");
        expectedJsonObject.put("httpCode", 404);

        Assert.assertEquals(expectedJsonObject.toString(),jsonObject.toString());

    }
}