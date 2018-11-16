package com.redbeemedia.enigma.core.json;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

public class JsonInputStreamParserTest {
    @Test
    public void testJsonParsing() throws JSONException {
        JsonInputStreamParser jsonInputStreamParser = new JsonInputStreamParser();

        ByteArrayInputStream bais = new ByteArrayInputStream("{\"test\": \"value\", \"other\": 123}".getBytes(Charset.forName("utf-8")));
        JSONObject jsonObject = jsonInputStreamParser.parse(bais);

        JSONObject expectedJsonObject = new JSONObject();
        expectedJsonObject.put("test", "value");
        expectedJsonObject.put("other", 123);

        Assert.assertEquals(expectedJsonObject.toString(),jsonObject.toString());
    }
}
