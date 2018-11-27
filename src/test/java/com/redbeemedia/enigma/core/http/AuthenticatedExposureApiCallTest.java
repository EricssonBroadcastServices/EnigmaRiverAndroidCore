package com.redbeemedia.enigma.core.http;

import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.session.MockSession;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AuthenticatedExposureApiCallTest {
    @Test
    public void testRequestMethod() {
        ISession session = new MockSession();
        AuthenticatedExposureApiCall getMethodNoBody = new AuthenticatedExposureApiCall("GET", session);
        Assert.assertEquals("GET", getMethodNoBody.getRequestMethod());

        AuthenticatedExposureApiCall postMethodNoBody = new AuthenticatedExposureApiCall("POST", session);
        Assert.assertEquals("POST", postMethodNoBody.getRequestMethod());

        AuthenticatedExposureApiCall getMethodWithBody = new AuthenticatedExposureApiCall("GET", session, new JSONObject());
        Assert.assertEquals("GET", getMethodWithBody.getRequestMethod());

        AuthenticatedExposureApiCall postMethodWithBody = new AuthenticatedExposureApiCall("POST", session, new JSONObject());
        Assert.assertEquals("POST", postMethodWithBody.getRequestMethod());
    }


    @Test
    public void testWriteToBody() throws JSONException, IOException {
        ISession session = new MockSession();
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("type", "JUnitTest");
        jsonBody.put("name", "testWriteToBody");

        {
            AuthenticatedExposureApiCall getMethod = new AuthenticatedExposureApiCall("GET", session, jsonBody);
            String body = getBody(getMethod);
            Assert.assertEquals("{\"name\":\"testWriteToBody\",\"type\":\"JUnitTest\"}", new JSONObject(body).toString());
        }

        {
            AuthenticatedExposureApiCall postMethod = new AuthenticatedExposureApiCall("POST", session, jsonBody);
            String body = getBody(postMethod);
            Assert.assertEquals("{\"name\":\"testWriteToBody\",\"type\":\"JUnitTest\"}", new JSONObject(body).toString());
        }
    }

    private static String getBody(IHttpCall httpCall) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        httpCall.writeBodyTo(byteArrayOutputStream);
        return new String(byteArrayOutputStream.toByteArray());
    }
}
