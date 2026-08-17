/*
 * SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
 *
 * SPDX-License-Identifier: MIT
 */
package com.redbeemedia.enigma.core.player;

import static com.redbeemedia.enigma.core.playrequest.MaterialProfile.PARAM_KEY;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.format.EnigmaMediaFormat;
import com.redbeemedia.enigma.core.http.AuthenticatedExposureApiCall;
import com.redbeemedia.enigma.core.http.IHttpConnection;
import com.redbeemedia.enigma.core.playrequest.AdobePrimetime;
import com.redbeemedia.enigma.core.playrequest.MaterialProfile;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.UrlPath;
import com.redbeemedia.enigma.core.util.function.IConsumer;
import com.redbeemedia.enigma.core.util.function.IConsumerTwo;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayRequest {
    private final ISession session;
    private final String assetId;
    private final Map<String, ?> adInsertionParameters;
    private final String adobePrimeToken;
    private final Set<EnigmaMediaFormat> supportedFormats;
    private final MaterialProfile materialProfile;

    public PlayRequest(
            ISession session,
            String assetId,
            Map<String, ?> adInsertionParameters,
            String adobePrimeToken,
            Set<EnigmaMediaFormat> supportedFormats,
            MaterialProfile materialProfile
    ) {
        this.session = session;
        this.assetId = assetId;
        this.adInsertionParameters = adInsertionParameters;
        this.adobePrimeToken = adobePrimeToken;
        this.supportedFormats = supportedFormats;
        this.materialProfile = materialProfile;
    }

    public void call(IConsumer<EnigmaError> onError, IConsumerTwo<JSONObject, IConsumer<EnigmaError>> onSuccess) throws MalformedURLException {
        UrlPath path = session.getBusinessUnit().getApiBaseUrl("v2").append("entitlement").append(assetId).append("play");

        if (adInsertionParameters != null) {
            path = path.appendQueryStringParameters(adInsertionParameters);
        }

        // device parameters
        path = path.appendQueryStringParameters(EnigmaRiverContext.getDeviceParameters().getParameters());
        path = appendSupportedFormatsAndDRMs(path);

        if (materialProfile != null) {
            path = path.append(PARAM_KEY + materialProfile.name());
        }

        URL url = path.toURL();
        AuthenticatedExposureApiCall apiCall = new AuthenticatedExposureApiCall("GET", session) {
            @Override
            public void prepare(IHttpConnection connection) {
                if (adobePrimeToken != null) {
                    connection.setHeader(AdobePrimetime.HTTP_HEADER_KEY, adobePrimeToken);
                }
                super.prepare(connection);
            }
        };

        EnigmaRiverContext.getHttpHandler().doHttp(url, apiCall, new PlayResponseHandler(assetId) {
            @Override
            protected void onError(EnigmaError error) {
                onError.accept(error);
            }

            @Override
            protected void onSuccess(JSONObject jsonObject) {
                onSuccess.accept(jsonObject, onError);
            }
        });
    }

    private UrlPath appendSupportedFormatsAndDRMs(UrlPath path) {
        Set<EnigmaMediaFormat.StreamFormat> formats = new HashSet<>();
        Set<EnigmaMediaFormat.DrmTechnology> drms = new HashSet<>();
        for (EnigmaMediaFormat mediaFormat : supportedFormats) {
            formats.add(mediaFormat.getStreamFormat());
            drms.add(mediaFormat.getDrmTechnology());
        }
        String formatsBuilder = buildFormats(formats);
        String drmBuilder = buildDrm(drms);
        Map<String, String> paramMap = new HashMap<>();
        if (!formatsBuilder.isEmpty()) {
            paramMap.put(SUPPORTED_FORMATS, formatsBuilder);
        }
        if (!drmBuilder.trim().isEmpty()) {
            paramMap.put(SUPPORTED_DRMS, drmBuilder);
        }
        if (!paramMap.isEmpty()) {
            path = path.appendQueryStringParameters(paramMap);
        }
        return path;
    }

    private String buildFormats(Set<EnigmaMediaFormat.StreamFormat> formats) {
        StringBuilder formatsBuilder = new StringBuilder();
        if (formats.contains(EnigmaMediaFormat.StreamFormat.DASH)) {
            formatsBuilder.append("dash").append(",");
        }
        if (formats.contains(EnigmaMediaFormat.StreamFormat.HLS)) {
            formatsBuilder.append("hls").append(",");
        }
        if (formats.contains(EnigmaMediaFormat.StreamFormat.MP3)) {
            formatsBuilder.append("mp3").append(",");
        }
        if (formats.contains(EnigmaMediaFormat.StreamFormat.SMOOTHSTREAMING)) {
            formatsBuilder.append("smoothstreaming").append(",");
        }
        return removeLastComma(formatsBuilder);
    }

    private String buildDrm(Set<EnigmaMediaFormat.DrmTechnology> drms) {
        StringBuilder drmBuilder = new StringBuilder();
        if (drms.contains(EnigmaMediaFormat.DrmTechnology.WIDEVINE)) {
            drmBuilder.append("widevine").append(",");
        }
        if (drms.contains(EnigmaMediaFormat.DrmTechnology.PLAYREADY)) {
            drmBuilder.append("playready").append(",");
        }
        if (drms.contains(EnigmaMediaFormat.DrmTechnology.FAIRPLAY)) {
            drmBuilder.append("fairplay").append(",");
        }
        return removeLastComma(drmBuilder);
    }

    private String removeLastComma(StringBuilder drmBuilder) {
        String drmBuilderStr = drmBuilder.toString();
        if (drmBuilderStr.endsWith(",")) {
            drmBuilderStr = drmBuilderStr.substring(0, drmBuilderStr.length() - 1);
        }
        return drmBuilderStr;
    }

    public static final String SUPPORTED_FORMATS = "supportedFormats";
    public static final String SUPPORTED_DRMS = "supportedDrms";
}
