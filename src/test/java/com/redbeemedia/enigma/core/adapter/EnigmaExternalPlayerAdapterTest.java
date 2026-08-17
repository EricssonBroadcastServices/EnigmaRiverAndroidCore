/*
 * SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
 *
 * SPDX-License-Identifier: MIT
 */
package com.redbeemedia.enigma.core.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.redbeemedia.enigma.core.analytics.AnalyticsPlayResponseData;
import com.redbeemedia.enigma.core.analytics.AnalyticsReporter;
import com.redbeemedia.enigma.core.analytics.IPlayerAnalyticsListener;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.NoInternetConnectionError;
import com.redbeemedia.enigma.core.error.NoSessionRejectionError;
import com.redbeemedia.enigma.core.error.NoSupportedMediaFormatsError;
import com.redbeemedia.enigma.core.error.ServerError;
import com.redbeemedia.enigma.core.error.ServerTimeoutError;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;
import com.redbeemedia.enigma.core.network.MockNetworkMonitor;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.task.MockTaskFactoryProvider;
import com.redbeemedia.enigma.core.time.Duration;
import com.redbeemedia.enigma.core.time.ITimeProvider;
import com.redbeemedia.enigma.core.time.MockTimeProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.regex.Pattern;

public class EnigmaExternalPlayerAdapterTest {
    private static final String ASSET_ID = "asset-id";
    private static final String MANIFEST_URL = "http://example.com/manifest.mpd";
    private static final String MANIFEST_URL_DASH_WIDEVINE = "http://example.com/manifest-dash-widevine.mpd";
    private static final String MANIFEST_URL_DASH_UNENC = "http://example.com/manifest-dash.mpd";
    private static final String MANIFEST_URL_HLS_WIDEVINE = "http://example.com/manifest-hls-widevine.m3u8";
    private static final String MANIFEST_URL_HLS_UNENC = "http://example.com/manifest-hls.m3u8";
    private static final String MANIFEST_URL_SMOOTHSTREAMING = "http://example.com/manifest.ism";
    private static final Pattern PLAY_URL_PATTERN = Pattern.compile(".*/entitlement/.*/play.*");
    private static final Pattern AKAMAI_TIME_URL_PATTERN = Pattern.compile("https://time\\.akamai\\.com/.*");

    @Test
    public void prepareReturnsFirstPlayableManifestUrl() throws JSONException {
        MockHttpHandler httpHandler = resetContext();
        queuePlayResponse(httpHandler, newDashUnencFormat(MANIFEST_URL));
        queueAkamaiTimeResponse(httpHandler);

        RecordingPrepareCallback callback = new RecordingPrepareCallback();
        newAdapter().prepare(ASSET_ID, callback);

        callback.assertPreparedOnce();
        assertEquals(MANIFEST_URL, callback.manifestUrl);
        callback.assertNoError();
    }

    @Test
    public void prepareSkipsFormatsWithEmptyOrMissingManifestUrls() throws JSONException {
        MockHttpHandler httpHandler = resetContext();
        queuePlayResponse(
                httpHandler,
                newDashUnencFormat(""),                    // empty – skip
                new JSONObject().put("format", "DASH"),  // missing mediaLocator field – skip
                newDashUnencFormat(MANIFEST_URL));                   // valid – use this
        queueAkamaiTimeResponse(httpHandler);

        RecordingPrepareCallback callback = new RecordingPrepareCallback();
        newAdapter().prepare(ASSET_ID, callback);

        callback.assertPreparedOnce();
        assertEquals(MANIFEST_URL, callback.manifestUrl);
        callback.assertNoError();
    }

    @Test
    public void prepareReportsPlayRequestErrorWithoutPreparedCallback() {
        MockHttpHandler httpHandler = resetContext();
        httpHandler.queueResponse(
                PLAY_URL_PATTERN,
                new HttpStatus(500, "Internal error"),
                "{\"httpCode\":500,\"message\":\"SERVER_ERROR\"}");

        RecordingPrepareCallback callback = new RecordingPrepareCallback();
        newAdapter().prepare(ASSET_ID, callback);

        callback.assertNotPrepared();
        callback.assertErrorOnce(ServerError.class);
    }

    @Test
    public void prepareReportsMissingSession() {
        resetContext();

        RecordingPrepareCallback callback = new RecordingPrepareCallback();
        newAdapter(null, new MockTimeProvider()).prepare(ASSET_ID, callback);

        callback.assertNotPrepared();
        callback.assertErrorOnce(NoSessionRejectionError.class);
    }

    @Test
    public void prepareReportsNoInternetWithoutPreparedCallback() {
        MockNetworkMonitor networkMonitor = new MockNetworkMonitor().setInternetAccess(false);
        resetContext(new MockHttpHandler(), networkMonitor);

        RecordingPrepareCallback callback = new RecordingPrepareCallback();
        new NoWaitNetworkRetryAdapter(new MockSession(), new TestAnalyticsListener(), new MockTimeProvider())
                .prepare(ASSET_ID, callback);

        callback.assertNotPrepared();
        callback.assertErrorOnce(NoInternetConnectionError.class);
    }

    @Test
    public void prepareReportsTimeServiceFailure() {
        resetContext();

        RecordingPrepareCallback callback = new RecordingPrepareCallback();
        newAdapter(new MockSession(), new NotReadyTimeProvider()).prepare(ASSET_ID, callback);

        callback.assertNotPrepared();
        callback.assertErrorOnce(ServerTimeoutError.class);
    }

    @Test
    public void prepareReportsNoSupportedMediaFormats() throws JSONException {
        MockHttpHandler httpHandler = resetContext();
        queuePlayResponse(httpHandler);

        RecordingPrepareCallback callback = new RecordingPrepareCallback();
        newAdapter().prepare(ASSET_ID, callback);

        callback.assertNotPrepared();
        callback.assertErrorOnce(NoSupportedMediaFormatsError.class);
    }

    @Test
    public void prepareForDownloadedAssetReturnsNullManifestUrl() {
        resetContext();

        RecordingPrepareCallback callback = new RecordingPrepareCallback();
        newAdapter().prepareForDownloadedAsset(
                ASSET_ID,
                "play-session-id",
                "https://analytics.example.com",
                "cdn-provider",
                callback);

        callback.assertPreparedOnce();
        assertNull(callback.manifestUrl);
        callback.assertNoError();
    }

    @Test
    public void prefersDashWidevineOverDashUnencrypted() throws JSONException {
        MockHttpHandler httpHandler = resetContext();
        queuePlayResponse(
                httpHandler,
                newDashUnencFormat(MANIFEST_URL_DASH_UNENC),
                newDashWidevineFormat(MANIFEST_URL_DASH_WIDEVINE));
        queueAkamaiTimeResponse(httpHandler);

        RecordingPrepareCallback callback = new RecordingPrepareCallback();
        newAdapter().prepare(ASSET_ID, callback);

        callback.assertPreparedOnce();
        assertEquals(MANIFEST_URL_DASH_WIDEVINE, callback.manifestUrl);
        callback.assertNoError();
    }

    @Test
    public void prefersDashOverHls() throws JSONException {
        MockHttpHandler httpHandler = resetContext();
        queuePlayResponse(
                httpHandler,
                newHlsUnencFormat(MANIFEST_URL_HLS_UNENC),
                newDashUnencFormat(MANIFEST_URL_DASH_UNENC));
        queueAkamaiTimeResponse(httpHandler);

        RecordingPrepareCallback callback = new RecordingPrepareCallback();
        newAdapter().prepare(ASSET_ID, callback);

        callback.assertPreparedOnce();
        assertEquals(MANIFEST_URL_DASH_UNENC, callback.manifestUrl);
        callback.assertNoError();
    }

    @Test
    public void selectsHighestPriorityFormatWhenAllFormatsPresent() throws JSONException {
        MockHttpHandler httpHandler = resetContext();
        queuePlayResponse(
                httpHandler,
                newHlsUnencFormat(MANIFEST_URL_HLS_UNENC),
                newHlsWidevineFormat(MANIFEST_URL_HLS_WIDEVINE),
                newDashUnencFormat(MANIFEST_URL_DASH_UNENC),
                newDashWidevineFormat(MANIFEST_URL_DASH_WIDEVINE));
        queueAkamaiTimeResponse(httpHandler);

        RecordingPrepareCallback callback = new RecordingPrepareCallback();
        newAdapter().prepare(ASSET_ID, callback);

        callback.assertPreparedOnce();
        assertEquals(MANIFEST_URL_DASH_WIDEVINE, callback.manifestUrl);
        callback.assertNoError();
    }

    @Test
    public void ignoresUnsupportedSmoothstreamingFormat() throws JSONException {
        MockHttpHandler httpHandler = resetContext();
        queuePlayResponse(
                httpHandler,
                newSmoothstreamingFormat(MANIFEST_URL_SMOOTHSTREAMING),
                newDashUnencFormat(MANIFEST_URL_DASH_UNENC));
        queueAkamaiTimeResponse(httpHandler);

        RecordingPrepareCallback callback = new RecordingPrepareCallback();
        newAdapter().prepare(ASSET_ID, callback);

        callback.assertPreparedOnce();
        assertEquals(MANIFEST_URL_DASH_UNENC, callback.manifestUrl);
        callback.assertNoError();
    }

    private static MockHttpHandler resetContext() {
        MockHttpHandler httpHandler = new MockHttpHandler();
        resetContext(httpHandler, new MockNetworkMonitor());
        return httpHandler;
    }

    private static void resetContext(MockHttpHandler httpHandler, MockNetworkMonitor networkMonitor) {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization()
                .setHttpHandler(httpHandler)
                .setNetworkMonitor(networkMonitor)
                .setTaskFactoryProvider(new MockTaskFactoryProvider()));
    }

    private static EnigmaExternalPlayerAdapter newAdapter() {
        return newAdapter(new MockSession(), new MockTimeProvider());
    }

    private static EnigmaExternalPlayerAdapter newAdapter(ISession session, ITimeProvider timeProvider) {
        return new EnigmaExternalPlayerAdapter(
                session,
                new TestAnalyticsListener(),
                null,
                null,
                null,
                timeProvider) {

            @Override
            protected boolean isDrmSupported() {
                return true;
            }
        };
    }

    private static void queuePlayResponse(MockHttpHandler httpHandler, JSONObject... formats) throws JSONException {
        JSONArray formatArray = new JSONArray();
        for (JSONObject format : formats) {
            formatArray.put(format);
        }
        JSONObject playResponse = new JSONObject();
        playResponse.put("formats", formatArray);
        httpHandler.queueResponseOk(PLAY_URL_PATTERN, playResponse.toString());
    }

    // region --- Format factory helpers ----------------------------------------------

    private static JSONObject newDashUnencFormat(String manifestUrl) throws JSONException {
        JSONObject format = new JSONObject();
        format.put("format", "DASH");
        format.put("mediaLocator", manifestUrl);
        return format;
    }

    private static JSONObject newDashWidevineFormat(String manifestUrl) throws JSONException {
        JSONObject format = new JSONObject();
        format.put("format", "DASH");
        format.put("mediaLocator", manifestUrl);
        JSONObject drm = new JSONObject();
        drm.put("com.widevine.alpha", new JSONObject());
        format.put("drm", drm);
        return format;
    }

    private static JSONObject newHlsUnencFormat(String manifestUrl) throws JSONException {
        JSONObject format = new JSONObject();
        format.put("format", "HLS");
        format.put("mediaLocator", manifestUrl);
        return format;
    }

    private static JSONObject newHlsWidevineFormat(String manifestUrl) throws JSONException {
        JSONObject format = new JSONObject();
        format.put("format", "HLS");
        format.put("mediaLocator", manifestUrl);
        JSONObject drm = new JSONObject();
        drm.put("com.widevine.alpha", new JSONObject());
        format.put("drm", drm);
        return format;
    }

    private static JSONObject newSmoothstreamingFormat(String manifestUrl) throws JSONException {
        JSONObject format = new JSONObject();
        format.put("format", "SMOOTHSTREAMING");
        format.put("mediaLocator", manifestUrl);
        return format;
    }

    // endregion --- Format factory helpers ----------------------------------------------

    private static void queueAkamaiTimeResponse(MockHttpHandler httpHandler) {
        httpHandler.queueResponseOk(AKAMAI_TIME_URL_PATTERN, "0");
    }

    private static class RecordingPrepareCallback implements IEnigmaExternalPlayerAdapter.PrepareCallback {
        private int preparedCalls = 0;
        private int errorCalls = 0;
        private String manifestUrl;
        private EnigmaError error;

        @Override
        public void onPrepared(String manifestUrl, IEnigmaExternalPlayerDrmSession drmSession) {
            preparedCalls++;
            this.manifestUrl = manifestUrl;
        }

        @Override
        public void onError(EnigmaError error) {
            errorCalls++;
            this.error = error;
        }

        private void assertPreparedOnce() {
            assertEquals(1, preparedCalls);
        }

        private void assertNotPrepared() {
            assertEquals(0, preparedCalls);
        }

        private void assertNoError() {
            assertEquals(0, errorCalls);
            assertNull(error);
        }

        private void assertErrorOnce(Class<? extends EnigmaError> expectedErrorClass) {
            assertEquals(1, errorCalls);
            assertTrue("Expected " + expectedErrorClass + " but got " + error, expectedErrorClass.isInstance(error));
        }
    }

    private static class TestAnalyticsListener implements IPlayerAnalyticsListener {
        private AnalyticsReporter reporter;

        @Override
        public void attach(
                AnalyticsReporter reporter,
                Runnable onPlaybackFinishedEvent,
                Runnable onPlaybackDestroyedEvent,
                AnalyticsPlayResponseData analyticsPlayResponseData,
                String playMode,
                String assetId) {
            this.reporter = reporter;
        }

        @Override
        public void detach() {
            reporter = null;
        }

        @Override
        public long getCurrentTimeInSeconds() {
            return 0;
        }

        @Override
        public String getTechnology() {
            return "UnitTestPlayer";
        }
    }

    private static class NoWaitNetworkRetryAdapter extends EnigmaExternalPlayerAdapter {
        NoWaitNetworkRetryAdapter(
                ISession session,
                IPlayerAnalyticsListener playerAnalyticsListener,
                ITimeProvider timeProvider) {
            super(session, playerAnalyticsListener, null, null, null, timeProvider);
        }

        @Override
        void waitBeforeNetworkRetry() {
        }

        @Override
        protected boolean isDrmSupported() {
            return true;
        }
    }

    private static class NotReadyTimeProvider implements ITimeProvider {
        @Override
        public long getTime() {
            return 0;
        }

        @Override
        public boolean isReady(Duration maxBlocktime) {
            return false;
        }
    }
}