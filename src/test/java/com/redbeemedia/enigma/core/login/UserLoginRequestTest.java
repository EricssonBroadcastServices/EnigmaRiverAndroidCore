// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.util.UrlPath;
import com.redbeemedia.enigma.core.util.device.MockDeviceInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UserLoginRequestTest {
    @Test
    public void testUserLoginRequestBody() throws IOException, JSONException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setDeviceInfo(new MockDeviceInfo()));
        UserLoginRequest userLoginRequest = new UserLoginRequest("matte", "secret_pass", new MockLoginResultHandler());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        userLoginRequest.writeBodyTo(byteArrayOutputStream);
        JSONObject bodyJson = new JSONObject(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));

        Assert.assertEquals("matte", bodyJson.getString("username"));
        Assert.assertEquals("secret_pass", bodyJson.getString("password"));
        Assert.assertTrue(bodyJson.has("deviceId"));
    }


    @Test
    public void testGetTargetUrl() throws MalformedURLException {
        UserLoginRequest userLoginRequest = new UserLoginRequest("matte", "secret_pass", new MockLoginResultHandler());
        UrlPath simpleBase = new UrlPath("https://myfakeurl.ericsson.com/api/v5/");
        Assert.assertEquals(new URL("https://myfakeurl.ericsson.com/api/v5/auth/login"), userLoginRequest.getTargetUrl(simpleBase).toURL());
    }
}
