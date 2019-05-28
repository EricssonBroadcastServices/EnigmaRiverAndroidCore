package com.redbeemedia.enigma.core.drm;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class DrmInfoTest {
    @Test
    public void testRequestProperties() {
        DrmInfo drmInfo = new DrmInfo("example.com", "mockPlayToken");
        Map<String,String> props = new HashMap<>();
        for(Map.Entry<String, String> requestProperty : drmInfo.getDrmKeyRequestProperties()) {
            props.put(requestProperty.getKey(), requestProperty.getValue());
        }
        Map<String,String> expected = new HashMap<>();
        expected.put("Authorization","Bearer mockPlayToken");
        Assert.assertEquals(expected, props);
    }

    @Test
    public void testNoPlayToken() {
        DrmInfo drmInfo = new DrmInfo("example.com", null);
        Map<String,String> props = new HashMap<>();
        for(Map.Entry<String, String> requestProperty : drmInfo.getDrmKeyRequestProperties()) {
            props.put(requestProperty.getKey(), requestProperty.getValue());
        }
        Assert.assertEquals(new HashMap<>(), props);
    }

    @Test
    public void testRequestId() {
        DrmInfo drmInfo = new DrmInfo("example.com", null, "mockRequestId");
        Map<String,String> props = new HashMap<>();
        for(Map.Entry<String, String> requestProperty : drmInfo.getDrmKeyRequestProperties()) {
            props.put(requestProperty.getKey(), requestProperty.getValue());
        }
        Map<String,String> expected = new HashMap<>();
        expected.put("X-Request-Id","mockRequestId");
        Assert.assertEquals(expected, props);
    }
}