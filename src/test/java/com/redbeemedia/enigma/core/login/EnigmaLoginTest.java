package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.testutil.Flag;
import com.redbeemedia.enigma.core.util.IHandler;
import com.redbeemedia.enigma.core.util.UrlPath;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EnigmaLoginTest {
    @Test
    public void testApiPath() throws MalformedURLException {
        EnigmaLogin enigmaLogin = new EnigmaLogin("cU", "bU");
        UrlPath url = enigmaLogin.getBusinessUnitBaseUrl(new UrlPath("http://example.com"));
        Assert.assertEquals(new URL("http://example.com/v1/customer/cU/businessunit/bU"), url.toURL());
    }

    @Test
    public void testLogin() throws MalformedURLException, JSONException {
        MockHttpHandler mockHandler = new MockHttpHandler();
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setExposureBaseUrl("https://example.com:8081").setHttpHandler(mockHandler));
        EnigmaLogin enigmaLogin = new EnigmaLogin("cU", "bU");
        enigmaLogin.login(new MockLoginRequest().addHeader("MockHeader","mocked"));

        List<String> mockHandlerLog = mockHandler.getLog();
        Assert.assertEquals(1,mockHandlerLog.size());

        JSONObject logEntry = new JSONObject(mockHandlerLog.get(0));
        Assert.assertEquals("POST", logEntry.getString("method"));
        Assert.assertEquals("https://example.com:8081/v1/customer/cU/businessunit/bU/auth/mock", logEntry.getString("url"));

        JSONObject headers = logEntry.getJSONObject("headers");
        Assert.assertEquals(1,headers.length());
        Assert.assertEquals("mocked",headers.getString("MockHeader"));
    }

    @Test
    public void testLoginWithUserLogin() throws MalformedURLException, JSONException {
        MockHttpHandler mockHandler = new MockHttpHandler();
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setExposureBaseUrl("https://example.com:8081").setHttpHandler(mockHandler));
        EnigmaLogin enigmaLogin = new EnigmaLogin("dev", "enigma");
        enigmaLogin.login(new UserLoginRequest("user", "passw0rd", new MockLoginResultHandler()));

        List<String> mockHandlerLog = mockHandler.getLog();
        Assert.assertEquals(1,mockHandlerLog.size());
        JSONObject logEntry = new JSONObject(mockHandlerLog.get(0));
        Assert.assertEquals("POST", logEntry.getString("method"));
        Assert.assertEquals("https://example.com:8081/v1/customer/dev/businessunit/enigma/auth/login", logEntry.getString("url"));

        JSONObject headers = logEntry.getJSONObject("headers");
        Assert.assertEquals(2,headers.length());
        Assert.assertEquals("application/json",headers.getString("Content-Type"));
        Assert.assertEquals("application/json",headers.getString("Accept"));

        JSONObject body = new JSONObject(logEntry.getString("body"));
        Assert.assertEquals("user", body.getString("username"));
        Assert.assertEquals("passw0rd", body.getString("password"));
        Assert.assertTrue(body.has("deviceId"));
    }


    @Test
    public void testCallbackHandler() throws MalformedURLException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        mockHttpHandler.queueResponse(new HttpStatus(200, "OK"));
        mockHttpHandler.queueResponse(new IllegalStateException());
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setExposureBaseUrl("https://example.com:8081").setHttpHandler(mockHttpHandler));

        MockHandler handler = new MockHandler();
        EnigmaLogin enigmaLogin = new EnigmaLogin("test", "test").setCallbackHandler(handler);

        final Flag onErrorCalled = new Flag();
        enigmaLogin.login(new UserLoginRequest("tester", "test", new MockLoginResultHandler() {
            @Override
            public void onError(Error error) {
                onErrorCalled.setFlag();
            }
        }));

        Assert.assertEquals(1, handler.runnables.size());
        Assert.assertFalse(onErrorCalled.isTrue());
        handler.runnables.get(0).run();
        Assert.assertTrue(onErrorCalled.isTrue());

        enigmaLogin.login(new MockLoginRequest());
        Assert.assertEquals(2, handler.runnables.size());
        handler.runnables.get(1).run();
    }

    private static class MockHandler implements IHandler {
        private List<Runnable> runnables = new ArrayList<>();
        @Override
        public boolean post(Runnable runnable) {
            runnables.add(runnable);
            return true;
        }
    }
}
