// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.util.UrlPath;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class ResumeLoginRequestTest {

    @Test
    public void testGetTargetUrl() throws MalformedURLException {
        ResumeLoginRequest resumeLoginRequest = new ResumeLoginRequest("sessionToken", new MockLoginResultHandler());
        UrlPath simpleBase = new UrlPath("https://myfakeurl.ericsson.com/api/v5/");
        Assert.assertEquals("https://myfakeurl.ericsson.com/api/v5/auth/session", resumeLoginRequest.getTargetUrl(simpleBase).toString());
    }
}
