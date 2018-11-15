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
        MockEnigmaRiverContext.resetInitialize(new EnigmaRiverContext.EnigmaRiverContextInitialization().setHttpHandler(mockHandler));
        EnigmaLogin enigmaLogin = new EnigmaLogin("cU", "bU");
        enigmaLogin.login(new MockLoginRequest());

        List<String> mockHandlerLog = mockHandler.getLog();
        Assert.assertEquals(1,mockHandlerLog.size());
        Assert.assertEquals("POST to https://psempexposureapi.ebsd.ericsson.net/v1/customer/cU/businessunit/bU/auth/mock {headers { Content-Type : application/json,Accept : application/json,}body { }}", mockHandlerLog.get(0));
    }

    @Test
    public void testLoginWithUserLogin() throws MalformedURLException {
        MockHttpHandler mockHandler = new MockHttpHandler();
        MockEnigmaRiverContext.resetInitialize(new EnigmaRiverContext.EnigmaRiverContextInitialization().setHttpHandler(mockHandler));
        EnigmaLogin enigmaLogin = new EnigmaLogin("dev", "enigma");
        enigmaLogin.login(new UserLoginRequest("user", "password"));

        List<String> mockHandlerLog = mockHandler.getLog();
        Assert.assertEquals(1,mockHandlerLog.size());
        Assert.assertEquals("POST to https://psempexposureapi.ebsd.ericsson.net/v1/customer/dev/businessunit/enigma/auth/login {headers { Content-Type : application/json,Accept : application/json,}body { }}", mockHandlerLog.get(0));
    }

}
