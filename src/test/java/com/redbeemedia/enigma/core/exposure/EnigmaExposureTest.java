package com.redbeemedia.enigma.core.exposure;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.testutil.Flag;
import com.redbeemedia.enigma.core.util.MockHandler;
import com.redbeemedia.enigma.core.util.UrlPath;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EnigmaExposureTest {
    @Test
    public void testDoRequest() throws JSONException {
        MockHttpHandler httpHandler = new MockHttpHandler();
        httpHandler.queueResponse(new HttpStatus(200,"OK"), "[\"a\",\"b\"]");
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(httpHandler).setExposureBaseUrl("http://testtesttest:12345"));
        EnigmaExposure enigmaExposure = new EnigmaExposure(new MockSession());

        final List<String> receivedResults = new ArrayList<>();
        Flag onSuccessCalled = new Flag();
        Flag onErrorCalled = new Flag();
        enigmaExposure.doRequest(new AbstractExposureRequest<List<String>>("POSTISH", AbstractExposureRequest.parseListMethod(String.class), new IExposureResultHandler<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                onSuccessCalled.setFlag();
                receivedResults.addAll(result);
            }

            @Override
            public void onError(Error error) {
                onErrorCalled.setFlag();
            }
        }) {
            @Override
            public UrlPath getUrl(ISession session) {
                return session.getApiBaseUrl().append("test/expo");
            }
        });

        onSuccessCalled.assertSet();
        onErrorCalled.assertNotSet();

        Assert.assertEquals(1, httpHandler.getLog().size());
        JSONObject logEntry = new JSONObject(httpHandler.getLog().get(0));
        Assert.assertEquals("POSTISH", logEntry.getString("method"));
        Assert.assertEquals("http://testtesttest:12345/v1/customer/mockCu/businessunit/mockBu/test/expo", logEntry.getString("url"));

        Assert.assertEquals(Arrays.asList("a","b"), receivedResults);
    }

    @Test
    public void testCallbackHandler() {
        MockHttpHandler httpHandler = new MockHttpHandler();
        httpHandler.queueResponse(new HttpStatus(200,"OK"), "[\"a\",\"b\"]");
        httpHandler.queueResponse(new RuntimeException("Fail"));
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(httpHandler).setExposureBaseUrl("http://testtesttest:12345"));
        MockHandler handler = new MockHandler();
        EnigmaExposure enigmaExposure = new EnigmaExposure(new MockSession()).setCallbackHandler(handler);

        final List<String> receivedResults = new ArrayList<>();
        Flag onSuccessCalled = new Flag();
        Flag onErrorCalled = new Flag();
        enigmaExposure.doRequest(new AbstractExposureRequest<List<String>>("POSTISH", AbstractExposureRequest.parseListMethod(String.class), new IExposureResultHandler<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                onSuccessCalled.setFlag();
                receivedResults.addAll(result);
            }

            @Override
            public void onError(Error error) {
                onErrorCalled.setFlag();
            }
        }) {
            @Override
            public UrlPath getUrl(ISession session) {
                return session.getApiBaseUrl().append("test/expo");
            }
        });

        Assert.assertEquals(1, handler.runnables.size());
        onSuccessCalled.assertNotSet();
        handler.runnables.get(0).run();
        onSuccessCalled.assertSet();

        onErrorCalled.assertNotSet();

        Flag onSuccessCalled2 = new Flag();
        Flag onErrorCalled2 = new Flag();
        enigmaExposure.doRequest(new AbstractExposureRequest<List<String>>("POSTISH", AbstractExposureRequest.parseListMethod(String.class), new IExposureResultHandler<List<String>>() {
            @Override
            public void onSuccess(List<String> result) {
                onSuccessCalled2.setFlag();
            }

            @Override
            public void onError(Error error) {
                onErrorCalled2.setFlag();
            }
        }) {
            @Override
            public UrlPath getUrl(ISession session) {
                return session.getApiBaseUrl().append("test2/expo");
            }
        });
        onSuccessCalled2.assertNotSet();
        onErrorCalled2.assertNotSet();

        Assert.assertEquals(2, handler.runnables.size());
        handler.runnables.get(1).run();

        onSuccessCalled2.assertNotSet();
        onErrorCalled2.assertSet();
    }
}
