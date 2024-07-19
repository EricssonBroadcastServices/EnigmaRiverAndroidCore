// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.businessunit.BusinessUnit;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.util.UrlPath;
import com.redbeemedia.enigma.core.util.device.MockDeviceInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class ApiKeyLoginRequestTest {
    @Test
    public void testGetTargetUrl() throws MalformedURLException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setExposureBaseUrl("http://exposure-mock-login/api/"));

        ApiKeyLoginRequest apiKeyLoginRequest = new ApiKeyLoginRequest("testUser_dh5d","secretApiKey",new MockLoginResultHandler());
        UrlPath targetUrl = apiKeyLoginRequest.getTargetUrl(new BusinessUnit("Mocky", "McMockface"));
        Assert.assertEquals("http://exposure-mock-login/api/v2/customer/Mocky/businessunit/McMockface/auth/session", targetUrl.toURL().toExternalForm());
    }

    @Test
    public void testPostBodyWithEnigmaLogin() throws JSONException {
        MockHttpHandler httpHandler = new MockHttpHandler();
        MockDeviceInfo mockDeviceInfo = new MockDeviceInfo() {
            @Override
            public String getDeviceId() {
                return "testWithEnigmaLogin_id";
            }

            @Override
            public String getName() {
                return "MockPhone S10";
            }
        };
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setDeviceInfo(mockDeviceInfo).setExposureBaseUrl("http://exposure-mock-login/api/").setHttpHandler(httpHandler));

        ApiKeyLoginRequest apiKeyLoginRequest = new ApiKeyLoginRequest("testUser_jd93","preSharedApiKey", new MockLoginResultHandler() {
            @Override
            public void onError(EnigmaError error) {
                error.printStackTrace();
                Assert.fail(error.toString());
            }
        });
        EnigmaLogin enigmaLogin = new EnigmaLogin(new BusinessUnit("Boom", "Mill"));

        Assert.assertEquals(0, httpHandler.getLog().size());
        enigmaLogin.login(apiKeyLoginRequest);
        Assert.assertEquals(1, httpHandler.getLog().size());

        JSONObject logEntry = new JSONObject(httpHandler.getLog().get(0));
        Assert.assertEquals("POST", logEntry.getString("method"));
        Assert.assertEquals("http://exposure-mock-login/api/v2/customer/Boom/businessunit/Mill/auth/session", logEntry.getString("url"));
        Assert.assertNotNull(logEntry.getJSONObject("headers"));
        Assert.assertEquals("preSharedApiKey", logEntry.getJSONObject("headers").getString("EMP-Auth"));
        JSONObject body = new JSONObject(logEntry.getString("body"));
        Assert.assertEquals("testUser_jd93", body.getString("username"));
        JSONObject device = body.getJSONObject("device");
        Assert.assertNotNull(device);
        Assert.assertEquals("testWithEnigmaLogin_id", device.getString("deviceId"));
        Assert.assertEquals("MockPhone S10", device.getString("name"));
    }
}
