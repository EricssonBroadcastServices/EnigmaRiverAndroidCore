package com.redbeemedia.enigma.core.epg.impl;

import com.redbeemedia.enigma.core.businessunit.BusinessUnit;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.epg.request.EpgRequest;
import com.redbeemedia.enigma.core.epg.response.IEpgResponse;
import com.redbeemedia.enigma.core.epg.response.IEpgResponseHandler;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpCall;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.util.ISO8601Util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.TimeZone;

public class DirectEpgTest {
    private ISO8601Util.IISO8601Writer iso8601formatter = ISO8601Util.newWriter(TimeZone.getTimeZone("UTC"));

    @Test
    public void testBackendCall() {
        final Counter doHttpCalled = new Counter();
        IHttpHandler httpHandler = new IHttpHandler() {
            @Override
            public void doHttp(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) {
                Assert.assertEquals("https://testBackendCall.DirectEpgTest/baseurl/v2/customer/mocker/businessunit/McMockface/epg/aMockChannel/date/2345-06-07?daysBackward=3&daysForward=3&pageSize=10000&pageNumber=1", url.toString());
                JSONArray results = new JSONArray();
                try {
                    JSONObject result = new JSONObject();
                    result.put("channelId", "aMockChannel");
                    JSONArray programs = new JSONArray();
                    result.put("programs", programs);
                    {
                        JSONObject program = new JSONObject();
                        program.put("assetId", "mockAsset");
                        program.put("startTime", iso8601formatter.toIso8601(11847455550000L));
                        program.put("endTime", iso8601formatter.toIso8601(11847465550000L));
                        programs.put(program);
                    }
                    results.put(result);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                byte[] data = results.toString().getBytes(StandardCharsets.UTF_8);
                responseHandler.onResponse(new HttpStatus(200, "OK"), new ByteArrayInputStream(data));
                doHttpCalled.count();
            }

            @Override
            public void doHttpBlocking(URL url, IHttpCall httpCall, IHttpResponseHandler responseHandler) throws InterruptedException {
                Assert.fail("Unexpected call to doHttpBlocking");
            }
        };
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(httpHandler).setExposureBaseUrl("https://testBackendCall.DirectEpgTest/baseurl"));

        DirectEpg directEpg = new DirectEpg(new BusinessUnit("mocker", "McMockface"));
        long centerDateMillis = 11847456550000L;
        long span = 1000L*60L*60L*24L*3L;
        final Counter onSuccessCalled = new Counter();
        long startUtcInRequest = centerDateMillis-span;
        directEpg.getPrograms(new EpgRequest("aMockChannel", startUtcInRequest, centerDateMillis+span), new IEpgResponseHandler() {
            @Override
            public void onSuccess(IEpgResponse epgResponse) {
                Assert.assertEquals(1, epgResponse.getPrograms().size());
                Assert.assertEquals(Duration.millis(10000000L), epgResponse.getPrograms().get(0).getDuration());
                Assert.assertEquals(startUtcInRequest, epgResponse.getStartUtcMillis());
                onSuccessCalled.count();
            }

            @Override
            public void onError(EnigmaError error) {
                Assert.fail(error.getTrace());
            }
        });
        doHttpCalled.assertOnce();
        onSuccessCalled.assertOnce();
    }
}
