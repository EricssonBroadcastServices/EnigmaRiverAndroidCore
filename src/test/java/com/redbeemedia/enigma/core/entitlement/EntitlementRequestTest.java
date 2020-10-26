package com.redbeemedia.enigma.core.entitlement;

import com.redbeemedia.enigma.core.businessunit.BusinessUnit;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.entitlement.EntitlementRequest;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.session.Session;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;

public class EntitlementRequestTest {
    @Test
    public void testHttpCall() throws MalformedURLException, JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setExposureBaseUrl("https://mocky.example.com/base"));
        EntitlementRequest entitlementRequest = new EntitlementRequest(new Session("mockSessToken123", new BusinessUnit("myCU", "myBU")), "mockAssetIDz");

        MockHttpHandler httpHandler = new MockHttpHandler();
        entitlementRequest.doHttpCall(httpHandler, new IHttpHandler.IHttpResponseHandler() {
            @Override
            public void onResponse(HttpStatus httpStatus) {
                Assert.fail("Not expected to be called");
            }

            @Override
            public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
                Assert.fail("Not expected to be called");
            }

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        });

        List<String> log = httpHandler.getLog();
        Assert.assertEquals(1, log.size());
        JSONObject logEntry = new JSONObject(log.get(0));
        Assert.assertEquals("https://mocky.example.com/base/v2/customer/myCU/businessunit/myBU/entitlement/mockAssetIDz/entitle", logEntry.getString("url"));
        Assert.assertEquals("Bearer mockSessToken123", logEntry.getJSONObject("headers").getString("Authorization"));
    }

    @Test
    public void testHttpCallWithTime() throws MalformedURLException, JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setExposureBaseUrl("https://mockful.example.com/base"));
        EntitlementRequest entitlementRequest = new EntitlementRequest(new Session("mockSessToken123", new BusinessUnit("myCU", "myBusnus")), "mockAssetIDz");
        entitlementRequest.setTime(1569325020000L);

        MockHttpHandler httpHandler = new MockHttpHandler();
        entitlementRequest.doHttpCall(httpHandler, new IHttpHandler.IHttpResponseHandler() {
            @Override
            public void onResponse(HttpStatus httpStatus) {
                Assert.fail("Not expected to be called");
            }

            @Override
            public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
                Assert.fail("Not expected to be called");
            }

            @Override
            public void onException(Exception e) {
                e.printStackTrace();
                Assert.fail(e.getMessage());
            }
        });

        List<String> log = httpHandler.getLog();
        Assert.assertEquals(1, log.size());
        JSONObject logEntry = new JSONObject(log.get(0));
        Assert.assertEquals("https://mockful.example.com/base/v2/customer/myCU/businessunit/myBusnus/entitlement/mockAssetIDz/entitle?time=2019-09-24T11:37:00Z", logEntry.getString("url"));
        Assert.assertEquals("Bearer mockSessToken123", logEntry.getJSONObject("headers").getString("Authorization"));
    }
}
