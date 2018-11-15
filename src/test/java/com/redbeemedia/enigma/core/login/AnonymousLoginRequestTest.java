package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.util.UrlPath;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class AnonymousLoginRequestTest {
    @Test
    public void testGetTargetUrl() throws MalformedURLException {
        AnonymousLoginRequest anonymousLoginRequest = new AnonymousLoginRequest();
        UrlPath simpleBase = new UrlPath("https://myfakeurl.ericsson.com/api/v5");
        Assert.assertEquals(new URL("https://myfakeurl.ericsson.com/api/v5/auth/anonymous"), anonymousLoginRequest.getTargetUrl(simpleBase).toURL());
    }
}
