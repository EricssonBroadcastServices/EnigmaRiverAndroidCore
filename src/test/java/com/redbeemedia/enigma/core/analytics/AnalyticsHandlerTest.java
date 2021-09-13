package com.redbeemedia.enigma.core.analytics;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.testutil.Flag;
import com.redbeemedia.enigma.core.time.MockTimeProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AnalyticsHandlerTest {

    private final AnalyticsPlayResponseData mockAnalyticsResponse = new AnalyticsPlayResponseData(new JSONObject(), "mock");
    @Test
    public void testInit() throws InterruptedException, AnalyticsException, JSONException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        mockHttpHandler.queueResponse(new HttpStatus(200, "OK"), "{\"receivedTime\": 123, \"repliedTime\": 151}");
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));

        ISession session = new MockSession();
        AnalyticsHandler analyticsHandler = new AnalyticsHandler(session,"pbs7", new MockTimeProvider(100), mockAnalyticsResponse);
        analyticsHandler.init();

        Assert.assertEquals(1, mockHttpHandler.getLog().size());
        JSONObject envelope = getEnvelope(mockHttpHandler.getLog().get(0));
        Assert.assertEquals("pbs7", envelope.getString("SessionId"));
        IBusinessUnit businessUnit = session.getBusinessUnit();
        Assert.assertEquals(businessUnit.getCustomerName(), envelope.getString("Customer"));
        Assert.assertEquals(businessUnit.getName(), envelope.getString("BusinessUnit"));

        Assert.assertEquals( Long.valueOf(-37),analyticsHandler.getClockOffsetForUnitTests());
    }


    @Test
    public void testEventBuffering() throws JSONException, InterruptedException, AnalyticsException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        mockHttpHandler.queueResponse(new HttpStatus(200, "OK"));
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));

        MockTimeProvider timeProvider = new MockTimeProvider(0);
        AnalyticsHandler analyticsHandler = new AnalyticsHandler(new MockSession(),"pbs1", timeProvider, mockAnalyticsResponse);
        analyticsHandler.onAnalytics(newEvent("Test1"));
        timeProvider.addTime(100);
        analyticsHandler.onAnalytics(newEvent("Test2"));
        timeProvider.addTime(100);
        analyticsHandler.onAnalytics(newEvent("Test3"));
        timeProvider.addTime(100);
        Assert.assertEquals(0, mockHttpHandler.getLog().size());

        analyticsHandler.sendData();
        Assert.assertEquals(1, mockHttpHandler.getLog().size());
        JSONObject envelope = getEnvelope(mockHttpHandler.getLog().get(0));
        Assert.assertEquals("pbs1", envelope.getString("SessionId"));
        JSONArray payload = envelope.getJSONArray("Payload");
        Assert.assertEquals(3, payload.length());
        Assert.assertEquals("Test1", payload.getJSONObject(0).getString("EventType"));
        Assert.assertEquals("Test2", payload.getJSONObject(1).getString("EventType"));
        Assert.assertEquals("Test3", payload.getJSONObject(2).getString("EventType"));
        Assert.assertTrue(envelope.has("DispatchTime"));

        analyticsHandler.sendData();
        Assert.assertEquals("No new events -> no http call.",1, mockHttpHandler.getLog().size());
    }

    @Test
    public void testRetry() throws JSONException, InterruptedException, AnalyticsException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        mockHttpHandler.queueResponse(new HttpStatus(500, "Internal error"));
        mockHttpHandler.queueResponse(new HttpStatus(200, "OK"));
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));

        AnalyticsHandler analyticsHandler = new AnalyticsHandler(new MockSession(),"pbs1", new MockTimeProvider(0), mockAnalyticsResponse);

        analyticsHandler.onAnalytics(newEvent("AnalyticsTest1"));
        analyticsHandler.onAnalytics(newEvent("AnalyticsTest2"));

        Flag exceptionThrown = new Flag();
        try {
            analyticsHandler.sendData();
        } catch (AnalyticsException e) {
            exceptionThrown.setFlag();
        }
        exceptionThrown.assertSet("No exception thrown.");

        analyticsHandler.onAnalytics(newEvent("Additional"));
        analyticsHandler.sendData();
        Assert.assertEquals(2, mockHttpHandler.getLog().size());

        JSONObject envelope = getEnvelope(mockHttpHandler.getLog().get(1));
        JSONArray payload = envelope.getJSONArray("Payload");
        Assert.assertEquals(3,payload.length());

        AssertEvent(payload, 0, "AnalyticsTest1");
        AssertEvent(payload, 1, "AnalyticsTest2");
        AssertEvent(payload, 2, "Additional");
    }

    @Test
    public void testFailedEventSend() throws JSONException, InterruptedException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        mockHttpHandler.queueResponse(new HttpStatus(500, "Internal error"));
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));

        AnalyticsHandler analyticsHandler = new AnalyticsHandler(new MockSession(),"pbs1", new MockTimeProvider(0), mockAnalyticsResponse);

        analyticsHandler.onAnalytics(newEvent("Test"));

        Flag exceptionThrown = new Flag();
        try {
            analyticsHandler.sendData();
        } catch (AnalyticsException e) {
            exceptionThrown.setFlag();
        }
        exceptionThrown.assertSet("No exception thrown");
    }

    @Test
    public void testAnalyticsUrl_IfCustomAnalyticsUrlIsNotSet() throws JSONException, InterruptedException, AnalyticsException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        mockHttpHandler.queueResponse(new HttpStatus(500, "Internal error"));
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));

        AnalyticsHandler analyticsHandler = new AnalyticsHandler(new MockSession(),"pbs1", new MockTimeProvider(0), mockAnalyticsResponse);
        assertThat(analyticsHandler.getSendUrl().toExternalForm(), is("https://mock.unittests.example.com/eventsink/send"));
    }

    @Test
    public void testAnalyticsUrl_IfCustomAnalyticsUrlIsSet() throws JSONException, InterruptedException, AnalyticsException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        mockHttpHandler.queueResponse(new HttpStatus(500, "Internal error"));
        MockEnigmaRiverContextInitialization initialization = new MockEnigmaRiverContextInitialization();
        initialization.setHttpHandler(mockHttpHandler);
        initialization.setAnalyticsUrl("https://customurl.com/");
        MockEnigmaRiverContext.resetInitialize(initialization);
        AnalyticsHandler analyticsHandler = new AnalyticsHandler(new MockSession(),"pbs1", new MockTimeProvider(0), mockAnalyticsResponse);
        assertThat(analyticsHandler.getSendUrl().toExternalForm(), is("https://customurl.com/eventsink/send"));
    }

    @Test
    public void testAnalyticsUrl_IfCustomInitAnalyticsUrlIsNotSet() throws JSONException, InterruptedException, AnalyticsException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        mockHttpHandler.queueResponse(new HttpStatus(500, "Internal error"));
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setHttpHandler(mockHttpHandler));

        AnalyticsHandler analyticsHandler = new AnalyticsHandler(new MockSession(),"pbs1", new MockTimeProvider(0), mockAnalyticsResponse);
        assertThat(analyticsHandler.getInitUrl().toExternalForm(), is("https://mock.unittests.example.com/eventsink/init"));
    }

    @Test
    public void testAnalyticsUrl_IfCustomInitAnalyticsUrlIsSet() throws JSONException, InterruptedException, AnalyticsException {
        MockHttpHandler mockHttpHandler = new MockHttpHandler();
        mockHttpHandler.queueResponse(new HttpStatus(500, "Internal error"));
        MockEnigmaRiverContextInitialization initialization = new MockEnigmaRiverContextInitialization();
        initialization.setHttpHandler(mockHttpHandler);
        initialization.setAnalyticsUrl("https://customurl.com/");
        MockEnigmaRiverContext.resetInitialize(initialization);
        AnalyticsHandler analyticsHandler = new AnalyticsHandler(new MockSession(),"pbs1", new MockTimeProvider(0), mockAnalyticsResponse);
        assertThat(analyticsHandler.getInitUrl().toExternalForm(), is("https://customurl.com/eventsink/init"));
    }


    private static void AssertEvent(JSONArray payload, int index, String expected) throws JSONException {
        Assert.assertEquals(expected, payload.getJSONObject(index).getString("EventType"));
    }

    private static JSONObject getEnvelope(String httpLog) throws JSONException {
        return new JSONObject(new JSONObject(httpLog).getString("body"));
    }

    private static JSONObject newEvent(String eventType) throws JSONException {
        JSONObject event = new JSONObject();
        event.put("EventType", eventType);
        return event;
    }
}
