package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.util.UrlPath;
import com.redbeemedia.enigma.core.util.device.IDeviceInfo;
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

public class AnonymousLoginRequestTest {
    @Test
    public void testGetTargetUrl() throws MalformedURLException {
        AnonymousLoginRequest anonymousLoginRequest = new AnonymousLoginRequest(new MockLoginResultHandler());
        UrlPath simpleBase = new UrlPath("https://myfakeurl.ericsson.com/api/v5");
        Assert.assertEquals(new URL("https://myfakeurl.ericsson.com/api/v5/auth/anonymous"), anonymousLoginRequest.getTargetUrl(simpleBase).toURL());
    }

    @Test
    public void testPostBody() throws IOException, JSONException {
        IDeviceInfo deviceInfo = new MockDeviceInfo();
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setDeviceInfo(deviceInfo));
        AnonymousLoginRequest anonymousLoginRequest = new AnonymousLoginRequest(new MockLoginResultHandler());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        anonymousLoginRequest.writeBodyTo(byteArrayOutputStream);
        JSONObject bodyJson = new JSONObject(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));

        Assert.assertEquals(deviceInfo.getDeviceId(), bodyJson.getString("deviceId"));
        Assert.assertEquals(deviceInfo.getType(), bodyJson.getJSONObject("device").getString("type"));
    }
}
