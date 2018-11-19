package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.util.UrlPath;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class EnigmaLoginTest {
    @Test
    public void testApiPath() throws MalformedURLException {
        EnigmaLogin enigmaLogin = new EnigmaLogin("cU", "bU");
        UrlPath url = enigmaLogin.getBusinessUnitBaseUrl(new UrlPath("http://example.com"));
        Assert.assertEquals(new URL("http://example.com/v1/customer/cU/businessunit/bU"), url.toURL());
    }

    @Test
    public void testLogin() throws MalformedURLException {
        MockHttpHandler mockHandler = new MockHttpHandler();
        MockEnigmaRiverContext.resetInitialize(new EnigmaRiverContext.EnigmaRiverContextInitialization().setExposureBaseUrl("https://example.com:8081").setHttpHandler(mockHandler));
        EnigmaLogin enigmaLogin = new EnigmaLogin("cU", "bU");
        enigmaLogin.login(new MockLoginRequest().addHeader("MockHeader","mocked"));

        List<String> mockHandlerLog = mockHandler.getLog();
        Assert.assertEquals(1,mockHandlerLog.size());
        Assert.assertEquals("POST to https://example.com:8081/v1/customer/cU/businessunit/bU/auth/mock {headers { MockHeader : mocked,}body { }}", mockHandlerLog.get(0));
    }

    @Test
    public void testLoginWithUserLogin() throws MalformedURLException {
        MockHttpHandler mockHandler = new MockHttpHandler();
        MockEnigmaRiverContext.resetInitialize(new EnigmaRiverContext.EnigmaRiverContextInitialization().setExposureBaseUrl("https://example.com:8081").setHttpHandler(mockHandler));
        EnigmaLogin enigmaLogin = new EnigmaLogin("dev", "enigma");
        enigmaLogin.login(new UserLoginRequest("user", "password", new MockLoginResultHandler()));

        List<String> mockHandlerLog = mockHandler.getLog();
        Assert.assertEquals(1,mockHandlerLog.size());
        Assert.assertEquals("POST to https://example.com:8081/v1/customer/dev/businessunit/enigma/auth/login {headers { Content-Type : application/json,Accept : application/json,}body { {\"deviceId\":\"9159b635e19ff412\",\"device\":{\"height\":1794,\"width\":1080,\"model\":\"Android SDK built for x86\",\"name\":\"\",\"os\":\"Android\",\"osVersion\":\"9\",\"manufacturer\":\"Google\",\"deviceId\":\"9159b635e19ff412\",\"type\":\"MOBILE\"},\"rememberMe\":true,\"username\":\"user\",\"password\":\"password\"}}}", mockHandlerLog.get(0));
    }

}
