package com.redbeemedia.enigma.core.http;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MockHttpHandler implements IHttpHandler {
    private List<String> log = new ArrayList<>();

    @Override
    public void doHttp(URL url, IHttpPreparator preparator, IHttpResponseHandler response) {
        try {
            MockHttpConnection mockHttpConnection = new MockHttpConnection();
            preparator.prepare(mockHttpConnection);
            JSONObject logEntry = new JSONObject();
            logEntry.put("method", preparator.getRequestMethod());
            logEntry.put("url", url.toString());
            JSONObject headerMap = new JSONObject();
            for (Map.Entry<String, String> header : mockHttpConnection.getHeaders().entrySet()) {
                headerMap.put(header.getKey(), header.getValue());
            }
            logEntry.put("headers", headerMap);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            preparator.writeBodyTo(byteArrayOutputStream);
            logEntry.put("body", new String(byteArrayOutputStream.toByteArray(), "utf-8"));
            log.add(logEntry.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getLog() {
        return log;
    }
}
