package com.redbeemedia.enigma.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlPathTest {
    @Test
    public void testToURL() throws MalformedURLException {
        UrlPath urlPath = new UrlPath("https://google.com/path");
        Assert.assertEquals(new URL("https://google.com/path"), urlPath.toURL());
    }

    @Test
    public void testSubPath() throws MalformedURLException {
        String[] validBaseParts = new String[]{"https://google.com/path","https://google.com/path/"};
        String[] validSubParts = new String[]{"/something/extra/here", "something/extra/here"};

        for(String basePart : validBaseParts) {
            for(String subPart : validSubParts) {
                UrlPath baseUrl = new UrlPath(basePart);
                UrlPath urlPath = new UrlPath(baseUrl, subPart);
                Assert.assertEquals("Failed combining '"+basePart+"' and '"+subPart+"'",new URL("https://google.com/path/something/extra/here"), urlPath.toURL());
            }
        }
    }

    @Test
    public void testQuery() throws MalformedURLException {
        UrlPath basic = new UrlPath("http://www.google.com");
        UrlPath advanced = basic.append("v1/api").append("test");
        UrlPath withQuery = advanced.append("?hello=3").append("&bye=").append("6");
        Assert.assertEquals(new URL("http://www.google.com/v1/api/test?hello=3&bye=6").toString(), withQuery.toURL().toString());
    }
}
