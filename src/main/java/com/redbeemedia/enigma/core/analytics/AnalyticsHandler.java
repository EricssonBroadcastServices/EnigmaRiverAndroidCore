package com.redbeemedia.enigma.core.analytics;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.ExposureHttpError;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.time.ITimeProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Buffers analytics events for a session and sends them in an envelope to the backend.
 */
public class AnalyticsHandler implements IAnalyticsHandler {
    private static final String CUSTOMER = "Customer";
    private static final String BUSINESS_UNIT = "BusinessUnit";
    private static final String SESSION_ID = "SessionId";
    private static final String DISPATCH_TIME = "DispatchTime";
    private static final String PAYLOAD = "Payload";
    private static final String CLOCK_OFFSET = "ClockOffset";

    private final ISession session;
    private final String playbackSessionId;
    private final ITimeProvider timeProvider;
    private volatile Long clockOffset = null;

    private final Object eventsLock = new Object();
    private JSONArray events = new JSONArray();

    public AnalyticsHandler(ISession session, String playbackSessionId, ITimeProvider timeProvider) {
        this.session = session;
        this.playbackSessionId = playbackSessionId;
        this.timeProvider = timeProvider;
    }

    @Override
    public void onAnalytics(JSONObject jsonObject) {
        synchronized (eventsLock) {
            events.put(jsonObject);
        }
    }

    public void init() throws AnalyticsException, InterruptedException {
        JSONObject envelope = new JSONObject();
        try {
            envelope.put(CUSTOMER, session.getCustomerUnitName());
            envelope.put(BUSINESS_UNIT, session.getBusinessUnitName());
            envelope.put(SESSION_ID, playbackSessionId);
        } catch (JSONException e) {
            throw new AnalyticsException("Failed to construct envelope.", e);
        }

        //Here we actually want to do a synchronous http call since we are on a separate thread already.
        Response response = new Response();
        long initRequestTime = timeProvider.getTime();
        EnigmaRiverContext.getHttpHandler().doHttpBlocking(getInitUrl(), new AuthenticatedExposureApiCall("POST", session, envelope), response);
        assertOK(response);
        if(response.data == null) {
            throw new AnalyticsException("Server returned empty response.");
        }
        this.clockOffset = calculateClockOffset(initRequestTime, response.data);
    }

    private long calculateClockOffset(long initRequestTime, JSONObject response) throws AnalyticsException {
        long currentTime = timeProvider.getTime();
        try {
            return (currentTime - response.getLong("repliedTime") + initRequestTime - response.getLong("receivedTime")) / 2L;
        } catch (JSONException e) {
            throw new AnalyticsException("Could not calculate clock offset.",e);
        }
    }

    protected Long getClockOffsetForUnitTests() {
        return clockOffset;
    }

    public synchronized void sendData() throws AnalyticsException, InterruptedException {
        final JSONArray currentEvents;
        synchronized (eventsLock) {
            if(events.length() == 0) {
                return;
            }
            currentEvents = events;
            events = new JSONArray();
        }
        try {
            JSONObject envelope = new JSONObject();
            try {
                envelope.put(CUSTOMER, session.getCustomerUnitName());
                envelope.put(BUSINESS_UNIT, session.getBusinessUnitName());
                envelope.put(SESSION_ID, playbackSessionId);
                envelope.put(DISPATCH_TIME, timeProvider.getTime());
                envelope.put(PAYLOAD, currentEvents);
                if(clockOffset != null) {
                    envelope.put(CLOCK_OFFSET, clockOffset.longValue());
                }
            } catch (JSONException e) {
                throw new AnalyticsException(e);
            }

            //Here we actually want to do a synchronous http call since we are on a separate thread already.
            Response response = new Response();
            EnigmaRiverContext.getHttpHandler().doHttpBlocking(getSendUrl(), new AuthenticatedExposureApiCall("POST", session, envelope), response);
            assertOK(response);
        } catch (AnalyticsException e) {
            retryEvents(currentEvents, e);
            throw e;
        }
    }

    private static void assertOK(Response response) throws AnalyticsException {
        if(response.exception != null) {
            throw new AnalyticsException(response.exception);
        } else {
            if(response.httpStatus.getResponseCode() != 200) {
                ExposureHttpError exposureHttpError = getExposureHttpError(response.data);
                throw new AnalyticsException("Server responded with "+response.httpStatus+ (exposureHttpError != null ? (" and "+exposureHttpError.toString()) : ""));
            }
        }
    }

    private static ExposureHttpError getExposureHttpError(JSONObject data) {
        if(data == null) {
            return null;
        } else {
            try {
                return new ExposureHttpError(data);
            } catch (JSONException e) {
                return null;
            }
        }
    }

    private void retryEvents(JSONArray eventsToRetry, Exception ex) {
        synchronized (eventsLock) {
            JSONArray newerEvents = events;
            events = eventsToRetry;
            try {
                for (int i = 0; i < newerEvents.length(); ++i) {
                    events.put(newerEvents.get(i));
                }
            } catch (JSONException e) {
                events = newerEvents; //Reset list to what is was
                ex.addSuppressed(e);
            }
        }
    }

    private URL getSendUrl() throws AnalyticsException {
        try {
            return EnigmaRiverContext.getExposureBaseUrl().append("eventsink/send").toURL();
        } catch (MalformedURLException e) {
            throw new AnalyticsException(e);
        }
    }

    private URL getInitUrl() throws AnalyticsException {
        try {
            return EnigmaRiverContext.getExposureBaseUrl().append("eventsink/init").toURL();
        } catch (MalformedURLException e) {
            throw new AnalyticsException(e);
        }
    }

    private static class Response implements IHttpHandler.IHttpResponseHandler {
        private volatile boolean callbackCalled = false;

        private Exception exception = null;
        private HttpStatus httpStatus = null;
        private JSONObject data = null;

        @Override
        public void onResponse(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
            onCallback();
        }


        @Override
        public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
            this.httpStatus = httpStatus;
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int read = -1;
                while ((read = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, read);
                }
                data = new JSONObject(new String(byteArrayOutputStream.toByteArray(),StandardCharsets.UTF_8));
                onCallback();
            } catch (Exception e) {
                onException(e);
            }
        }

        @Override
        public void onException(Exception e) {
            exception = e;
            onCallback();
        }

        private void onCallback() {
            if(callbackCalled) {
                throw new IllegalStateException("Callback called multiple times");
            }
            this.callbackCalled = true;
        }
    }
}