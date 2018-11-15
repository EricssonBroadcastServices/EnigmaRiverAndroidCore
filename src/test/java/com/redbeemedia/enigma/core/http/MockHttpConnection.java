package com.redbeemedia.enigma.core.http;

import java.util.LinkedHashMap;
import java.util.Map;

public class MockHttpConnection implements IHttpConnection {
    private Map<String,String> headers = new LinkedHashMap<>();

    @Override
    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }
}
