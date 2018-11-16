package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.util.UrlPath;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class UserLoginRequestTest {
    @Test
    public void testUserLoginRequest() {
        UserLoginRequest userLoginRequest = new UserLoginRequest("matte", "secret_pass", new MockLoginResultHandler());
        //TODO
    }


    @Test
    public void testGetTargetUrl() throws MalformedURLException {
        UserLoginRequest userLoginRequest = new UserLoginRequest("matte", "secret_pass", new MockLoginResultHandler());
        UrlPath simpleBase = new UrlPath("https://myfakeurl.ericsson.com/api/v5/");
        Assert.assertEquals(new URL("https://myfakeurl.ericsson.com/api/v5/auth/login"), userLoginRequest.getTargetUrl(simpleBase).toURL());

    }
}
