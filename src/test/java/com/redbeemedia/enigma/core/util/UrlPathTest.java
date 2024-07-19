// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

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

    private class TestClass {

        String value;

        public TestClass(String value) { this.value = value; }

        @Override
        public String toString() { return value; }
    }
    @Test
    public void testQueryStringParameters() throws MalformedURLException {
        HashMap<String, String> stringParameters = new HashMap<>();
        stringParameters.put("p1", "xyz");
        stringParameters.put("p2", "1");
        stringParameters.put("should_not_be_included", null);

        HashMap<String, Float> floatParameters = new HashMap<>();
        floatParameters.put("p1", 42f);
        floatParameters.put("p2", 65536.0f);

        Assert.assertEquals("?p1=xyz&p2=1", new UrlPath("").appendQueryStringParameters(stringParameters).toString());

        String baseUrl = "http://www.example.com/";
        Assert.assertEquals(baseUrl + "path?p1=xyz&p2=1", new UrlPath(baseUrl).append("path").appendQueryStringParameters(stringParameters).toURL().toString());
        Assert.assertEquals(baseUrl + "path?p1=42.0&p2=65536.0", new UrlPath(baseUrl).append("path").appendQueryStringParameters(floatParameters).toURL().toString());
        Assert.assertEquals(baseUrl + "path?foo=bar&p1=xyz&p2=1", new UrlPath(baseUrl + "path?foo=bar").appendQueryStringParameters(stringParameters).toURL().toString());
        Assert.assertEquals(baseUrl + "?dog?cat&p1=xyz&p2=1", new UrlPath(baseUrl).append("?dog?cat").appendQueryStringParameters(stringParameters).toURL().toString());
        Assert.assertEquals(baseUrl + "?addr=https://www.com&p1=xyz&p2=1&p1=42.0&p2=65536.0", new UrlPath(baseUrl + "?addr=https://www.com").appendQueryStringParameters(stringParameters).appendQueryStringParameters(floatParameters).toURL().toString());

        HashMap<String, TestClass> stringTestClassHashMap = new HashMap<>();
        stringTestClassHashMap.put("p1", new TestClass("cat"));
        stringTestClassHashMap.put("p2", new TestClass("dog"));

        Assert.assertEquals(baseUrl + "path?p1=cat&p2=dog", new UrlPath(baseUrl).append("path").appendQueryStringParameters(stringTestClassHashMap).toURL().toString());


    }
}
