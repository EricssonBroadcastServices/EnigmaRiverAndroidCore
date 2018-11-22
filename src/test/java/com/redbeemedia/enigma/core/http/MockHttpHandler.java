package com.redbeemedia.enigma.core.http;

import com.redbeemedia.enigma.core.http.mockresponses.MockOnExceptionResponse;
import com.redbeemedia.enigma.core.http.mockresponses.MockOnResponseNoInputstreamResponse;
import com.redbeemedia.enigma.core.http.mockresponses.MockOnResponseResponse;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MockHttpHandler implements IHttpHandler {
    private Queue<IHttpHandler> responses = new LinkedList<>();
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
            if(!responses.isEmpty()) {
                responses.poll().doHttp(url, preparator, response);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void queueResponse(IHttpHandler response) {
        this.responses.add(response);
    }

    public void queueResponse(HttpStatus httpStatus) {
        this.queueResponse(new MockOnResponseNoInputstreamResponse(httpStatus));
    }

    public void queueResponse(Exception exception) {
        this.queueResponse(new MockOnExceptionResponse(exception));
    }

    public void queueResponse(HttpStatus httpStatus, String responseBody) {
        try {
            this.queueResponse(new MockOnResponseResponse(httpStatus, responseBody.getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<String> getLog() {
        return log;
    }
}
