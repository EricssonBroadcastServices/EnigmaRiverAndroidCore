package com.redbeemedia.enigma.core.analytics;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.time.ITimeProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Buffers analytics events for a session and sends them in an envelope to the backend.
 */
public class AnalyticsHandler extends AbstractAnalyticsHandler {

    public AnalyticsHandler(ISession session, String playbackSessionId, ITimeProvider timeProvider, AnalyticsPlayResponseData analyticsPlayResponseData) {
        super(session, playbackSessionId, timeProvider, analyticsPlayResponseData);
    }

    @Override
    public synchronized void sendData() throws AnalyticsException, InterruptedException {
        boolean sendAnalytics = shouldSendAnalytics(analyticsPlayResponseData.analyticsPercentage);
        if (!sendAnalytics) {
            return;
        }
        final JSONArray currentEvents;
        synchronized (eventsLock) {
            if (events.length() == 0) {
                return;
            }
            currentEvents = events;
            events = new JSONArray();
        }
        if(playbackSessionId == null){
            return;
        }
        try {
            JSONObject envelope = new JSONObject();
            try {
                IBusinessUnit businessUnit = session.getBusinessUnit();
                envelope.put(CUSTOMER, businessUnit.getCustomerName());
                envelope.put(BUSINESS_UNIT, businessUnit.getName());
                envelope.put(SESSION_ID, playbackSessionId);
                envelope.put(DISPATCH_TIME, timeProvider.getTime());
                envelope.put(PAYLOAD, currentEvents);

                if (clockOffset != null) {
                    envelope.put(CLOCK_OFFSET, clockOffset.longValue());
                }
            } catch (JSONException e) {
                throw new AnalyticsException(e);
            }

            //Here we actually want to do a synchronous http call since we are on a separate thread already.
            Response response = new Response();
            EnigmaRiverContext.getHttpHandler().doHttpBlocking(getSendUrl(), new AuthenticatedExposureApiCall("POST", session, envelope), response);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            assertOK(response);
        } catch (AnalyticsException e) {
            retryEvents(currentEvents, e);
            throw e;
        }
    }

    @Override
    public boolean shouldSendEvent(JSONObject jsonObject) {
        return true;
    }
}
