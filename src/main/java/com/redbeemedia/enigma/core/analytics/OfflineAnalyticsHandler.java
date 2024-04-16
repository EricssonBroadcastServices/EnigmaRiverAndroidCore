package com.redbeemedia.enigma.core.analytics;

import android.util.Log;

import androidx.annotation.NonNull;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.time.ITimeProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Buffers analytics events for a session and sends them in an envelope to the backend.
 * <p>
 * MAP1: (offlineEventsMap)
 * KEY1 : AssetID
 * VALUE1: MAP2
 *
 * <p>
 * MAP2:
 * KEY2 : PLAYBACKSESSIONID
 * VALUE2: List of EVENTS
 */
public class OfflineAnalyticsHandler extends AbstractAnalyticsHandler {

    public static final String OFFLINE_ANALYTICS_EVENTS = "OFFLINE_ANALYTICS_EVENTS";
    public static final String OFFLINE_ANALYTICS_TAG = "OFFLINE_ANALYTICS_TAG";
    public static final String OFFLINE_ANALYTICS_URL = "OFFLINE_ANALYTICS_URL";

    private final String assetId;
    private String baseUrl;
    private final List<String> validCompletedEventNames = new ArrayList<>();
    private final List<String> validOfflineEventNames = new ArrayList<>();
    public static final int UPDATE_FREQUENCY_5_MIN = 5 * 60 * 1000;

    private static final Object sendDataLock = new Object();

    public OfflineAnalyticsHandler(String assetId, ISession session, String playbackSessionId, ITimeProvider timeProvider, AnalyticsPlayResponseData analyticsPlayResponseData, String analyticsBaseUrl) {
        super(session, playbackSessionId, timeProvider, analyticsPlayResponseData);
        this.assetId = assetId;
        this.baseUrl = analyticsBaseUrl;
        createValidEvents();
    }

    public OfflineAnalyticsHandler(ISession session, ITimeProvider timeProvider, AnalyticsPlayResponseData analyticsPlayResponseData) {
        super(session, null, timeProvider, analyticsPlayResponseData);
        byte[] analyticsUrlBytes = EnigmaRiverContext.getEnigmaStorageManager().load(OFFLINE_ANALYTICS_URL);
        if (analyticsUrlBytes != null) {
            this.baseUrl = new String(analyticsUrlBytes, StandardCharsets.UTF_8);
        }
        this.assetId = null;
        createValidEvents();
    }

    /*
    This method will be called at 2 places
    1. Background thread run every 5mins : BackgroundAnalyticsWorker
    2. AnalyticsReporter, sendData when user playback the asset
     */
    @Override
    public void sendData() {
        synchronized (sendDataLock){
            if (!setupAnalyticsUrl()) {
                return;
            }
            // Store the events
            JSONObject allOfflineAssetIdEventsBucket = storeAndGetEvents();
            try {
                // If network, then try to send data
                if (EnigmaRiverContext.getNetworkMonitor().hasInternetAccess() && allOfflineAssetIdEventsBucket != null) {
                    sendEventsToServer(allOfflineAssetIdEventsBucket);
                    updateEventsInStorage(allOfflineAssetIdEventsBucket);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("INFO", "Error while sending Offline events", e);
            }
        }

    }

    private boolean setupAnalyticsUrl() {
        if (baseUrl == null) {
            byte[] analyticsUrlBytes = EnigmaRiverContext.getEnigmaStorageManager().load(OFFLINE_ANALYTICS_URL);
            if (analyticsUrlBytes != null) {
                this.baseUrl = new String(analyticsUrlBytes, StandardCharsets.UTF_8);
            }else{
                return false;
            }
        }
        return true;
    }

    private JSONObject storeAndGetEvents() {
        final JSONArray currentEvents;
        synchronized (eventsLock) {
            currentEvents = events;
            events = new JSONArray();
        }
        try {
            JSONObject allOfflineAssetIdEventsBucket = getOfflineStoredEvents();
            if (assetId != null) {
                JSONArray offlineEventsArray = new JSONArray();
                JSONObject playbackSessionMap = allOfflineAssetIdEventsBucket.optJSONObject(assetId);
                if (playbackSessionMap == null) {
                    playbackSessionMap = new JSONObject();
                    playbackSessionMap.put(playbackSessionId, offlineEventsArray);
                    allOfflineAssetIdEventsBucket.put(assetId, playbackSessionMap);
                } else {
                    JSONArray jsonArray = playbackSessionMap.optJSONArray(playbackSessionId);
                    if (jsonArray == null) {
                        playbackSessionMap.put(playbackSessionId, offlineEventsArray);
                    } else {
                        offlineEventsArray = jsonArray;
                    }
                }
                for (int i = 0; i < currentEvents.length(); i++) {
                    offlineEventsArray.put(currentEvents.get(i));
                }
                updateEventsInStorage(allOfflineAssetIdEventsBucket);
            }
            return allOfflineAssetIdEventsBucket;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Offline Analytics", "Error while sending offline message", e);
        }
        return null;
    }

    private void updateEventsInStorage(JSONObject offlineAssetIdEventsBucket) {
        // whatever is left stored to local storage
        String eventsAsString = offlineAssetIdEventsBucket.toString();
        byte[] bytes = eventsAsString.getBytes(StandardCharsets.UTF_8);
        try {
            EnigmaRiverContext.getEnigmaStorageManager().store(OFFLINE_ANALYTICS_EVENTS, bytes);
        } catch (Exception e) {
            // retry to save the data again
            EnigmaRiverContext.getEnigmaStorageManager().store(OFFLINE_ANALYTICS_EVENTS, bytes);
        }
    }

    @NonNull
    private JSONObject getOfflineStoredEvents() throws JSONException {
        JSONObject offlineEventsSessionMap = getStoredEvents();
        if (offlineEventsSessionMap == null) {
            offlineEventsSessionMap = new JSONObject();
        }
        return offlineEventsSessionMap;
    }

    @Override
    public boolean shouldSendEvent(JSONObject jsonObject) {
        boolean found = false;
        for (String eventType : validOfflineEventNames) {
            if (jsonObject.optString("EventType").equals(eventType)) {
                found = true;
                break;
            }
        }
        return found;
    }

    private void sendEventsToServer(JSONObject storedOfflineEventsMap) throws JSONException {
        Iterator<String> assetIdKeys = storedOfflineEventsMap.keys();
        while (assetIdKeys.hasNext()) {
            String thisAssetId = assetIdKeys.next();
            JSONObject thisPlaybackSessionIdMap = storedOfflineEventsMap.getJSONObject(thisAssetId);
            Iterator<String> playbackSessionIdkeys = thisPlaybackSessionIdMap.keys();
            List<String> sentPlaybackSessionIds = new ArrayList<>();
            while (playbackSessionIdkeys.hasNext()) {
                String thisPlaybackSessionId = playbackSessionIdkeys.next();
                if(thisPlaybackSessionId.isEmpty()){
                    continue;
                }
                JSONArray thisPlaySessionIdEvents = thisPlaybackSessionIdMap.getJSONArray(thisPlaybackSessionId);
                int totalEventsLength = thisPlaySessionIdEvents.length() - 1;
                if (totalEventsLength <= 0) {
                    continue;
                }
                JSONObject lastEventForThisPlaySession = thisPlaySessionIdEvents.getJSONObject(totalEventsLength);
                String lastEventType = lastEventForThisPlaySession.optString("EventType");
                boolean isSessionFinished = false;
                for (String validCompletedEvent : validCompletedEventNames) {
                    if (lastEventType.equalsIgnoreCase(validCompletedEvent)) {
                        isSessionFinished = true;
                        break;
                    }
                }
                if (!isSessionFinished) {
                    continue;
                }

                try {
                    JSONObject envelope = new JSONObject();
                    try {
                        IBusinessUnit businessUnit = session.getBusinessUnit();
                        envelope.put(CUSTOMER, businessUnit.getCustomerName());
                        envelope.put(BUSINESS_UNIT, businessUnit.getName());
                        envelope.put(SESSION_ID, thisPlaybackSessionId);
                        envelope.put(DISPATCH_TIME, timeProvider.getTime());
                        envelope.put(PAYLOAD, thisPlaySessionIdEvents);
                        if (clockOffset != null) {
                            envelope.put(CLOCK_OFFSET, clockOffset.longValue());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(OFFLINE_ANALYTICS_TAG, e.toString());
                    }
                    //Here we actually want to do a synchronous http call since we are on a separate thread already.
                    Response response = new Response();
                    EnigmaRiverContext.getHttpHandler().doHttpBlocking(
                            getOfflineSendUrl(baseUrl),
                            new AuthenticatedExposureApiCall("POST", session, envelope),
                            response);
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    assertOK(response);
                    Log.d(OFFLINE_ANALYTICS_TAG, "****** Sent offline event successfully ***** ");
                    sentPlaybackSessionIds.add(thisPlaybackSessionId);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w(OFFLINE_ANALYTICS_TAG, "Cannot send events to the analytics server");
                }
            }
            // remove toRemovePlaybackSessionId which are sent
            for (String toRemovePlaybackSessionId : sentPlaybackSessionIds) {
                thisPlaybackSessionIdMap.remove(toRemovePlaybackSessionId);
            }
        }
    }

    private JSONObject getStoredEvents() {
        byte[] storedEvents = EnigmaRiverContext.getEnigmaStorageManager().load(OFFLINE_ANALYTICS_EVENTS);
        JSONObject storedOfflineEventsMap = null;
        if (storedEvents != null) {
            String storedEventsOfflineMap = new String(storedEvents, StandardCharsets.UTF_8);
            try {
                storedOfflineEventsMap = new JSONObject(storedEventsOfflineMap);
            } catch (JSONException e) {
                Log.w("Offline Analytics", e.getMessage());
            }
        }
        return storedOfflineEventsMap;
    }

    private void createValidEvents() {
        validOfflineEventNames.add(new AnalyticsEvents.AnalyticsDeviceInfoEvent().getName());
        validOfflineEventNames.add(new AnalyticsEvents.AnalyticsCreatedEvent().getName());
        validOfflineEventNames.add(new AnalyticsEvents.AnalyticsPlayerReadyEvent().getName());
        validOfflineEventNames.add(new AnalyticsEvents.AnalyticsStartedEvent().getName());
        validOfflineEventNames.add(new AnalyticsEvents.AnalyticsAbortedEvent().getName());
        validOfflineEventNames.add(new AnalyticsEvents.AnalyticsPausedEvent().getName());
        validOfflineEventNames.add(new AnalyticsEvents.AnalyticsCompletedEvent().getName());
        validOfflineEventNames.add(new AnalyticsEvents.AnalyticsErrorEvent().getName());
        validOfflineEventNames.add(new AnalyticsEvents.AnalyticsScrubbedToEvent().getName());
        validOfflineEventNames.add(new AnalyticsEvents.AnalyticsAppBackgroundedEvent().getName());
        validOfflineEventNames.add(new AnalyticsEvents.AnalyticsResumedEvent().getName());
        validOfflineEventNames.add(new AnalyticsEvents.AnalyticsGracePeriodEndedEvent().getName());
        validCompletedEventNames.add(new AnalyticsEvents.AnalyticsAbortedEvent().getName());
        validOfflineEventNames.add(new AnalyticsEvents.AnalyticsAppResumedEvent().getName());
        validCompletedEventNames.add(new AnalyticsEvents.AnalyticsCompletedEvent().getName());
        validCompletedEventNames.add(new AnalyticsEvents.AnalyticsErrorEvent().getName());
        validCompletedEventNames.add(new AnalyticsEvents.AnalyticsGracePeriodEndedEvent().getName());
    }
}
