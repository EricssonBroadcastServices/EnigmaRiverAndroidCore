package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.util.UrlPath;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

//TODO: change the name
public class ResumeLoginRequestTest {

    @Test
    public void testGetTargetUrl() throws MalformedURLException {
        ResumeLoginRequest resumeLoginRequest = new ResumeLoginRequest("Bearer " + "sessionToken", new MockLoginResultHandler());
        UrlPath simpleBase = new UrlPath("https://myfakeurl.ericsson.com/api/v5/");
        Assert.assertEquals(new URL("https://myfakeurl.ericsson.com/api/v5/auth/session"), resumeLoginRequest.getTargetUrl(simpleBase).toURL());
    }
}
