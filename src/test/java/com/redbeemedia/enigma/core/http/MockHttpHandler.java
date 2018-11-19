package com.redbeemedia.enigma.core.http;

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
            StringBuilder logBuilder = new StringBuilder();
            logBuilder.append(preparator.getRequestMethod()+" to ");
            logBuilder.append(url.toString());
            logBuilder.append(" {");
            logBuilder.append("headers { ");
            for (Map.Entry<String, String> header : mockHttpConnection.getHeaders().entrySet()) {
                logBuilder.append(header.getKey()).append(" : ").append(header.getValue()).append(",");
            }
            logBuilder.append("}");
            logBuilder.append("body { ");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            preparator.writeBodyTo(byteArrayOutputStream);
            logBuilder.append(new String(byteArrayOutputStream.toByteArray(), "utf-8"));
            logBuilder.append("}");
            logBuilder.append("}");
            log.add(logBuilder.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public List<String> getLog() {
        return log;
    }
}
