package com.redbeemedia.enigma.core.analytics;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.http.ExposureHttpError;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.util.UrlPath;

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
public abstract class AbstractAnalyticsHandler implements IBufferingAnalyticsHandler {
    protected static final String CUSTOMER = "Customer";
    protected static final String BUSINESS_UNIT = "BusinessUnit";
    protected static final String SESSION_ID = "SessionId";
    protected static final String DISPATCH_TIME = "DispatchTime";
    protected static final String PAYLOAD = "Payload";
    protected static final String CLOCK_OFFSET = "ClockOffset";

    protected static final String SEQUENCE_NUMBER = "SequenceNumber";
    protected static final String SEQUENCE_INTERVAL = "AnalyticsPostInterval";
    protected static final String PROVIDER = "CDNVendor";
    protected static final String ANALYTICS_TAG = "AnalyticsTag";
    protected static final String STREAMING_TECHNOLOGY = "StreamingTechnology";
    protected static final String BUCKET = "AnalyticsBucket";

    protected final ISession session;
    protected final String playbackSessionId;
    protected final ITimeProvider timeProvider;
    protected volatile Long clockOffset = null;
    protected long sequenceNumber = 0;
    protected AnalyticsPlayResponseData analyticsPlayResponseData;

    protected final Object eventsLock = new Object();
    protected volatile JSONArray events = new JSONArray();

    public AbstractAnalyticsHandler(ISession session, String playbackSessionId, ITimeProvider timeProvider, AnalyticsPlayResponseData analyticsPlayResponseData) {
        this.session = session;
        this.playbackSessionId = playbackSessionId;
        this.timeProvider = timeProvider;
        this.analyticsPlayResponseData = analyticsPlayResponseData;
    }

    public abstract void sendData() throws AnalyticsException, InterruptedException;
    public abstract boolean shouldSendEvent(JSONObject jsonObject);

    @Override
    public void onAnalytics(JSONObject jsonObject) {
        if(shouldSendEvent(jsonObject)) {
            synchronized (eventsLock) {
                if (analyticsPlayResponseData.initialized) {
                    try {
                        jsonObject.put(SEQUENCE_NUMBER, sequenceNumber++);
                        jsonObject.put(SEQUENCE_INTERVAL, analyticsPlayResponseData.postIntervalSeconds);
                        jsonObject.put(PROVIDER, analyticsPlayResponseData.provider);
                        jsonObject.put(ANALYTICS_TAG, analyticsPlayResponseData.tag);
                        jsonObject.put(STREAMING_TECHNOLOGY, analyticsPlayResponseData.streamingTechnology);
                        jsonObject.put(BUCKET, analyticsPlayResponseData.bucket);
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
                events.put(jsonObject);
            }
        }
    }

    static boolean shouldSendAnalytics(float analyticsPercentage) {
        double randomValue = Math.random() * 100;  //0.0 to 99.9
        return randomValue <= analyticsPercentage;
    }


    protected static void assertOK(Response response) throws AnalyticsException {
        if (response.exception != null) {
            throw new AnalyticsException(response.exception);
        } else {
            if (response.httpStatus == null) {
                throw new AnalyticsException("Got null http status");
            } else if (response.httpStatus.getResponseCode() != 200) {
                ExposureHttpError exposureHttpError = getExposureHttpError(response.data);
                throw new AnalyticsException("Server responded with " + response.httpStatus + (exposureHttpError != null ? (" and " + exposureHttpError.toString()) : ""));
            }
        }
    }

    protected static ExposureHttpError getExposureHttpError(JSONObject data) {
        if (data == null) {
            return null;
        } else {
            try {
                return new ExposureHttpError(data);
            } catch (JSONException e) {
                return null;
            }
        }
    }

    protected void retryEvents(JSONArray eventsToRetry, Exception ex) {
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

    protected URL getSendUrl() throws AnalyticsException {
        return createAnalyticsUr("eventsink/send", EnigmaRiverContext.getAnalyticsUrl());
    }

    protected URL getOfflineSendUrl(String baseUrl) throws AnalyticsException {
        UrlPath analyticsUrl = session.getBusinessUnit().createAnalyticsUrl(baseUrl);
        URL analyticsUr = createAnalyticsUr("eventsink/send", analyticsUrl);
        return analyticsUr;
    }

    protected URL getInitUrl() throws AnalyticsException {
        return createAnalyticsUr("eventsink/init", EnigmaRiverContext.getAnalyticsUrl());
    }

    protected URL createAnalyticsUr(String uri, UrlPath analyticsUrl) throws AnalyticsException {
        try {
            if (analyticsUrl == null) {
                // return Base url
                return session.getBusinessUnit().createAnalyticsUrl(null).toURL();
            } else {
                return analyticsUrl.append(uri).toURL();
            }
        } catch (MalformedURLException e) {
            throw new AnalyticsException(e);
        }
    }

    protected static class Response implements IHttpHandler.IHttpResponseHandler {
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
                String dataResponse = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
                if (!dataResponse.isEmpty()) {
                    data = new JSONObject(dataResponse);
                }
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
            if (callbackCalled) {
                throw new IllegalStateException("Callback called multiple times");
            }
            this.callbackCalled = true;
        }
    }
}
