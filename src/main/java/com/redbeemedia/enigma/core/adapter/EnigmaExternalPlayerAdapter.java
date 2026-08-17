/*
 * SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
 *
 * SPDX-License-Identifier: MIT
 */
package com.redbeemedia.enigma.core.adapter;

import static com.redbeemedia.enigma.core.analytics.OfflineAnalyticsHandler.OFFLINE_ANALYTICS_URL;
import static com.redbeemedia.enigma.core.format.EnigmaMediaFormat.DrmTechnology.NONE;
import static com.redbeemedia.enigma.core.format.EnigmaMediaFormat.DrmTechnology.WIDEVINE;
import static com.redbeemedia.enigma.core.format.EnigmaMediaFormat.StreamFormat.DASH;
import static com.redbeemedia.enigma.core.format.EnigmaMediaFormat.StreamFormat.HLS;
import static com.redbeemedia.enigma.core.player.AnalyticsUtil.setDeviceUtcTimeDifference;

import android.media.MediaDrm;
import android.util.Log;

import com.redbeemedia.enigma.core.analytics.AbstractAnalyticsHandler;
import com.redbeemedia.enigma.core.analytics.AnalyticsHandler;
import com.redbeemedia.enigma.core.analytics.AnalyticsPlayResponseData;
import com.redbeemedia.enigma.core.analytics.AnalyticsReporter;
import com.redbeemedia.enigma.core.analytics.IPlayerAnalyticsListener;
import com.redbeemedia.enigma.core.analytics.OfflineAnalyticsHandler;
import com.redbeemedia.enigma.core.context.AppForegroundListener;
import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.InvalidAssetError;
import com.redbeemedia.enigma.core.error.NoInternetConnectionError;
import com.redbeemedia.enigma.core.error.NoSessionRejectionError;
import com.redbeemedia.enigma.core.error.NoSupportedMediaFormatsError;
import com.redbeemedia.enigma.core.error.ServerTimeoutError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormatUtil;
import com.redbeemedia.enigma.core.format.IMediaFormatSelector;
import com.redbeemedia.enigma.core.format.IMediaFormatSupportSpec;
import com.redbeemedia.enigma.core.format.SimpleMediaFormatSelector;
import com.redbeemedia.enigma.core.network.BaseNetworkMonitorListener;
import com.redbeemedia.enigma.core.player.AnalyticsReportingTask;
import com.redbeemedia.enigma.core.player.EnigmaPlayer.EnigmaPlayerLifecycle;
import com.redbeemedia.enigma.core.player.HeartbeatRepeater;
import com.redbeemedia.enigma.core.player.IAnalyticsReportingTask;
import com.redbeemedia.enigma.core.player.JsonStreamInfo;
import com.redbeemedia.enigma.core.player.MediaType;
import com.redbeemedia.enigma.core.player.PlayRequest;
import com.redbeemedia.enigma.core.player.ServerTimeService;
import com.redbeemedia.enigma.core.playrequest.MaterialProfile;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.task.ITaskFactoryProvider;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.ITimeProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/*package-protected*/ class EnigmaExternalPlayerAdapter implements IEnigmaExternalPlayerAdapter {
    private static final String TAG = "EnigmaExtPlayerAdapter";

    private final EnigmaPlayerLifecycle lifecycle;
    private final ITimeProvider timeProvider;
    private final ISession session;
    private final String adobePrimeToken;
    private final Map<String, String> playRequestOptionalParameters;
    private final MaterialProfile materialProfile;
    private final IPlayerAnalyticsListener listener;
    private final ITaskFactoryProvider taskFactoryProvider;
    private long deviceUtcTimeDifference = 0L;
    private IAnalyticsReportingTask reportingTask;
    private AnalyticsReporter analyticsReporter;
    private HeartbeatRepeater heartbeatRepeater;
    private AppForegroundListener appForegroundListener;
    private BaseNetworkMonitorListener networkMonitorListener;
    private final List<EnigmaExternalAdMarker> adMarkers = new ArrayList<>();

    private static final IMediaFormatSelector DEFAULT_FORMAT_SELECTOR = new SimpleMediaFormatSelector(
            EnigmaMediaFormat.DASH().widevine(),
            EnigmaMediaFormat.DASH().unenc(),
            EnigmaMediaFormat.HLS().widevine(),
            EnigmaMediaFormat.HLS().unenc()
    );

    EnigmaExternalPlayerAdapter(
            ISession session,
            IPlayerAnalyticsListener playerAnalyticsListener,
            String adobePrimeToken,
            Map<String, String> playRequestOptionalParameters,
            MaterialProfile materialProfile) {
        this(
                session,
                playerAnalyticsListener,
                adobePrimeToken,
                playRequestOptionalParameters,
                materialProfile,
                null
        );
    }

    EnigmaExternalPlayerAdapter(
            ISession session,
            IPlayerAnalyticsListener playerAnalyticsListener,
            String adobePrimeToken,
            Map<String, String> playRequestOptionalParameters,
            MaterialProfile materialProfile,
            ITimeProvider timeProvider) {
        this.listener = playerAnalyticsListener;
        this.session = session;
        this.adobePrimeToken = adobePrimeToken;
        this.playRequestOptionalParameters = playRequestOptionalParameters;
        this.materialProfile = materialProfile;

        taskFactoryProvider = EnigmaRiverContext.getTaskFactoryProvider();
        lifecycle = new EnigmaPlayerLifecycle();
        if (timeProvider != null) {
            this.timeProvider = timeProvider;
        } else if (session != null) {
            this.timeProvider = new ServerTimeService(
                    session.getBusinessUnit(),
                    taskFactoryProvider.getTaskFactory(),
                    lifecycle);
        } else {
            this.timeProvider = null;
        }
        lifecycle.fireOnStart(null);
    }

    @Override
    public List<EnigmaExternalAdMarker> getAdMarkers() {
        return new ArrayList<>(adMarkers);
    }

    @Override
    public void prepare(String assetId, PrepareCallback callback) {
        stopAnalytics();

        if (session == null) {
            callback.onError(new NoSessionRejectionError());
            return;
        }

        if (!ensureNetworkAvailable(callback)) {
            return;
        }

        if (!ensureTimeServiceReady(callback)) {
            return;
        }

        Set<EnigmaMediaFormat> supportedFormats = buildSupportedFormats();
        IMediaFormatSupportSpec formatSupportSpec = buildFormatSupportSpec(supportedFormats);

        PlayRequest playRequest = new PlayRequest(
                session,
                assetId,
                playRequestOptionalParameters,
                adobePrimeToken,
                supportedFormats,
                materialProfile);

        try {
            playRequest.call(
                    enigmaError -> handlePlayRequestError(enigmaError, callback),
                    (jsonObject, enigmaErrorIConsumer) ->
                            handlePlayResponse(jsonObject, formatSupportSpec, assetId, callback)
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
            callback.onError(new InvalidAssetError(assetId, new UnexpectedError(e)));
        }
    }

    private boolean ensureNetworkAvailable(PrepareCallback callback) {
        if (EnigmaRiverContext.getNetworkMonitor().hasInternetAccess()) {
            return true;
        }

        try {
            waitBeforeNetworkRetry();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            callback.onError(new NoInternetConnectionError());
            return false;
        }

        if (!EnigmaRiverContext.getNetworkMonitor().hasInternetAccess()) {
            Log.w(TAG, "Cannot access network");
            callback.onError(new NoInternetConnectionError());
            return false;
        }

        return true;
    }

    private boolean ensureTimeServiceReady(PrepareCallback callback) {
        if (timeProvider.isReady(Duration.seconds(30))) {
            return true;
        }

        Log.w(TAG, "Could not start time service");
        callback.onError(new ServerTimeoutError("Could not start time service"));
        return false;
    }

    private Set<EnigmaMediaFormat> buildSupportedFormats() {
        HashSet<EnigmaMediaFormat> formats = new HashSet<>();
        formats.add(new EnigmaMediaFormat(DASH, NONE));
        formats.add(new EnigmaMediaFormat(HLS, NONE));
        if (isDrmSupported()) {
            formats.add(new EnigmaMediaFormat(DASH, WIDEVINE));
            formats.add(new EnigmaMediaFormat(HLS, WIDEVINE));
        }
        return formats;
    }

    private void handlePlayRequestError(EnigmaError enigmaError, PrepareCallback callback) {
        Log.d(TAG, enigmaError.getTrace());
        callback.onError(enigmaError);
    }

    private void handlePlayResponse(
            JSONObject jsonObject,
            IMediaFormatSupportSpec formatSupportSpec,
            String assetId,
            PrepareCallback callback) {
        try {
            JSONObject selectedFormat = selectPlayableManifestUrl(jsonObject, formatSupportSpec);
            if (selectedFormat == null) {
                callback.onError(new NoSupportedMediaFormatsError(
                        "Could not find a playable mediaLocator in the play response."));
                return;
            }

            String manifestUrl = selectedFormat.optString("mediaLocator", "");
            IEnigmaExternalPlayerDrmSession drmSession = parseGenericDrmSession(selectedFormat);

            String playbackSessionId = jsonObject.optString("playSessionId", UUID.randomUUID().toString());
            String cdnProvider = extractCdnProvider(jsonObject);

            boolean audioOnly = jsonObject.optBoolean("audioOnly", false);
            MediaType mediaType = audioOnly ? MediaType.AUDIO : MediaType.VIDEO;
            String playMode = new JsonStreamInfo(jsonObject.optJSONObject("streamInfo"), mediaType).getPlayMode();

            AnalyticsPlayResponseData analyticsInformation = new AnalyticsPlayResponseData(jsonObject, "", session);
            JSONObject adsInfoJson = jsonObject.optJSONObject("ads");

            AbstractAnalyticsHandler analyticsHandler = new AnalyticsHandler(
                    session,
                    playbackSessionId,
                    timeProvider,
                    analyticsInformation);

            setDeviceUtcTimeDifference((onDeviceTime) -> finalizePreparation(
                    onDeviceTime,
                    adsInfoJson,
                    analyticsHandler,
                    analyticsInformation,
                    assetId,
                    cdnProvider,
                    playMode,
                    manifestUrl,
                    drmSession,
                    callback));

        } catch (JSONException e) {
            e.printStackTrace();
            callback.onError(new UnexpectedError(e));
        }
    }

    private String extractCdnProvider(JSONObject jsonObject) {
        JSONObject cdnObject = jsonObject.optJSONObject("cdn");
        return cdnObject != null ? cdnObject.optString("provider", "") : null;
    }

    private void finalizePreparation(
            long onDeviceTime,
            JSONObject adsInfoJson,
            AbstractAnalyticsHandler analyticsHandler,
            AnalyticsPlayResponseData analyticsInformation,
            String assetId,
            String cdnProvider,
            String playMode,
            String manifestUrl,
            IEnigmaExternalPlayerDrmSession drmSession,
            PrepareCallback callback) {
        try {
            deviceUtcTimeDifference = onDeviceTime;

            if (adsInfoJson != null && adsInfoJson.length() > 0) {
                setupAdDetection(adsInfoJson);
            }

            startAnalytics(
                    analyticsHandler,
                    () -> callback.onPrepared(manifestUrl, drmSession),
                    analyticsInformation.postIntervalSeconds * 1000,
                    assetId,
                    cdnProvider,
                    playMode);
        } catch (RuntimeException e) {
            callback.onError(new UnexpectedError(e));
        }
    }

    private void setupAdDetection(JSONObject adsInfoJson) {
        adMarkers.clear();
        try {
            String stitcher = adsInfoJson.optString("stitcher");
            if ("CSAI".equalsIgnoreCase(stitcher)) {
                JSONArray markersArray = adsInfoJson.optJSONArray("adMarkers");
                if (markersArray != null) {
                    for (int i = 0; i < markersArray.length(); i++) {
                        JSONObject markerObj = markersArray.getJSONObject(i);
                        String type = markerObj.optString("type");
                        String tagType = markerObj.optString("tagType");
                        String adTag = markerObj.optString("adTag");

                        if (adTag != null && !adTag.isEmpty()) {
                            adMarkers.add(new EnigmaExternalAdMarker(type, tagType, adTag));
                        }
                    }
                    Log.d(TAG, "Successfully parsed " + adMarkers.size() + " ad tags from the backend.");
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing CSAI adMarkers", e);
        }
    }

    @Override
    public void prepareForDownloadedAsset(
            String assetId,
            String playbackSessionId,
            String analyticsBaseUrl,
            String cdnProvider,
            PrepareCallback callback) {
        stopAnalytics();

        if (session == null) {
            callback.onError(new NoSessionRejectionError());
            return;
        }

        try {
            byte[] analyticsBaseUrlBytes = analyticsBaseUrl.getBytes(StandardCharsets.UTF_8);
            EnigmaRiverContext.getEnigmaStorageManager().store(OFFLINE_ANALYTICS_URL, analyticsBaseUrlBytes);

            JSONObject offlineJsonObject = new JSONObject();
            JSONObject cdnJsonObject = new JSONObject();
            cdnJsonObject.put("host", cdnProvider);
            offlineJsonObject.put("cdn", cdnJsonObject);
            offlineJsonObject.put("analytics", new JSONObject());

            AnalyticsPlayResponseData analyticsInformation = new AnalyticsPlayResponseData(offlineJsonObject, null,
                    session);
            AbstractAnalyticsHandler analyticsHandler = new OfflineAnalyticsHandler(
                    assetId,
                    session,
                    playbackSessionId,
                    timeProvider,
                    analyticsInformation,
                    analyticsBaseUrl);

            startAnalytics(
                    analyticsHandler,
                    () -> callback.onPrepared(null, null),
                    UPDATE_FREQUENCY_5_MIN,
                    assetId,
                    cdnProvider,
                    OFFLINE_PLAY_MODE);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onError(new UnexpectedError(e));
        }
    }

    @Override
    public void release() {
        stopAnalytics();
        lifecycle.fireOnStop(null);
    }

    private void startAnalytics(
            AbstractAnalyticsHandler analyticsHandler,
            Runnable onPrepared,
            int updateFrequency,
            String assetId,
            String cdnProvider,
            String playMode) {
        reportingTask = new AnalyticsReportingTask(taskFactoryProvider.getTaskFactory(), analyticsHandler,
                updateFrequency);
        analyticsReporter = new AnalyticsReporter(
                timeProvider,
                analyticsHandler,
                deviceUtcTimeDifference);

        listener.attach(
                analyticsReporter,
                this::onPlaybackFinished,
                this::onPlaybackDestroyed,
                analyticsHandler.getAnalyticsPlayResponseData(),
                playMode,
                assetId
        );

        analyticsReporter.deviceInfo(cdnProvider, listener.getTechnology());
        analyticsReporter.playbackCreated(assetId);
        analyticsReporter.playbackHandshakeStarted(assetId);

        heartbeatRepeater = new HeartbeatRepeater(taskFactoryProvider, analyticsReporter, this::getOffsetTime);
        heartbeatRepeater.setEnabled(true);

        appForegroundListener = new AppForegroundListener() {
            @Override
            public void onForegrounded() {
                analyticsReporter.playbackAppResumed(getOffsetTime());
            }

            @Override
            public void onBackgrounded() {
                analyticsReporter.playbackAppBackgrounded(getOffsetTime());
            }
        };
        appForegroundListener.startListening();

        networkMonitorListener = new BaseNetworkMonitorListener() {
            @Override
            public void onConnectionTypeChanged(String connectionType) {
                if (analyticsReporter != null) {
                    analyticsReporter.playbackConnectionTypeChanged(getOffsetTime(), connectionType);
                }
            }
        };
        EnigmaRiverContext.getNetworkMonitor().addListener(networkMonitorListener);
        analyticsReporter.playbackConnectionTypeChanged(getOffsetTime(),
                EnigmaRiverContext.getNetworkMonitor().getConnectionType());

        reportingTask.start();
        onPrepared.run();
    }

    private JSONObject selectPlayableManifestUrl(
            JSONObject playResponse,
            IMediaFormatSupportSpec formatSupportSpec) throws JSONException {

        JSONArray formats = playResponse.optJSONArray("formats");
        if (formats == null) {
            return null;
        }

        JSONObject selectedFormat = EnigmaMediaFormatUtil.selectUsableMediaFormat(
                formats,
                formatSupportSpec,
                DEFAULT_FORMAT_SELECTOR);

        if (selectedFormat == null) {
            Log.w(TAG, "No supported media format found in play response");
            return null;
        }

        return selectedFormat;
    }

    private IEnigmaExternalPlayerDrmSession parseGenericDrmSession(JSONObject formatObject) {
        if (formatObject == null) return null;

        JSONObject drmObject = formatObject.optJSONObject("drm");
        if (drmObject == null) return null;

        JSONObject widevineObject = drmObject.optJSONObject(WIDEVINE.getKey());
        if (widevineObject == null) return null;

        String licenseUrl = widevineObject.optString("licenseServerUrl");
        if (licenseUrl.isEmpty()) return null;

        Map<String, String> headers = new HashMap<>();

        JSONObject propertiesJson = widevineObject.optJSONObject("properties");
        if (propertiesJson != null) {
            Iterator<String> keys = propertiesJson.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                headers.put(key, propertiesJson.optString(key));
            }
        }

        return new GenericExternalDrmSession(licenseUrl, headers);
    }

    private static IMediaFormatSupportSpec buildFormatSupportSpec(final Set<EnigmaMediaFormat> supportedFormats) {
        return new IMediaFormatSupportSpec() {
            @Override
            public boolean supports(EnigmaMediaFormat enigmaMediaFormat) {
                return supportedFormats.contains(enigmaMediaFormat);
            }

            @Override
            public Set<EnigmaMediaFormat> getSupportedFormats() {
                return supportedFormats;
            }
        };
    }

    void waitBeforeNetworkRetry() throws InterruptedException {
        Thread.sleep(NETWORK_RETRY_DELAY_MILLIS);
    }

    private void onPlaybackFinished() {
        heartbeatRepeater.setEnabled(false);
    }

    private void onPlaybackDestroyed() {
        stopAnalytics();
    }

    private void stopAnalytics() {
        if (reportingTask != null) {
            reportingTask.stop();
            reportingTask = null;
        }
        if (analyticsReporter != null) {
            analyticsReporter.playbackAborted(getOffsetTime());
            analyticsReporter = null;
        }
        if (heartbeatRepeater != null) {
            heartbeatRepeater.setEnabled(false);
            heartbeatRepeater = null;
        }
        if (appForegroundListener != null) {
            appForegroundListener.stopListening();
            appForegroundListener = null;
        }
        if (networkMonitorListener != null) {
            EnigmaRiverContext.getNetworkMonitor().removeListener(networkMonitorListener);
            networkMonitorListener = null;
        }
        listener.detach();
    }

    private long getOffsetTime() {
        return listener.getCurrentTimeInSeconds() * 1000;
    }

    protected boolean isDrmSupported() {
        return MediaDrm.isCryptoSchemeSupported(WIDEVINE_UUID);
    }

    /* package-protected */ static final String OFFLINE_PLAY_MODE = "offline";
    private static final int UPDATE_FREQUENCY_5_MIN = 5 * 60 * 1000;
    private static final int NETWORK_RETRY_DELAY_MILLIS = 5100;
    private static final UUID WIDEVINE_UUID = new UUID(0xEDEF8BA979D64ACEL, 0xA3C827DCD51D21EDL);
}