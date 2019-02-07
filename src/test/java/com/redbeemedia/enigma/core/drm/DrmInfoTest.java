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
}