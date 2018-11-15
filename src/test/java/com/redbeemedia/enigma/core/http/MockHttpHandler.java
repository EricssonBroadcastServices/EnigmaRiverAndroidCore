package com.redbeemedia.enigma.core.http;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MockHttpHandler implements IHttpHandler {
    private List<String> log = new ArrayList<>();

    @Override
    public void post(URL url, IHttpPreparator preparator, IHttpHandlerResponse response) {
        MockHttpConnection mockHttpConnection = new MockHttpConnection();
        preparator.prepare(mockHttpConnection);
        StringBuilder logBuilder = new StringBuilder();
        logBuilder.append("POST to ");
        logBuilder.append(url.toString());
        logBuilder.append(" {");
        logBuilder.append("headers { ");
        for(Map.Entry<String, String> header : mockHttpConnection.getHeaders().entrySet()) {
            logBuilder.append(header.getKey()).append(" : ").append(header.getValue()).append(",");
        }
        logBuilder.append("}");
        logBuilder.append("body { ");
        logBuilder.append("}");
        logBuilder.append("}");
        log.add(logBuilder.toString());
    }

    public List<String> getLog() {
        return log;
    }
}
