/*
 * SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
 *
 * SPDX-License-Identifier: MIT
 */
package com.redbeemedia.enigma.core.adapter;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.error.EnigmaError;

import java.util.List;

public interface IEnigmaExternalPlayerAdapter {
    interface PrepareCallback {
        void onPrepared(@Nullable String manifestUrl, IEnigmaExternalPlayerDrmSession drmSession);
        void onError(EnigmaError error);
    }

    void prepare(String assetId, PrepareCallback callback);

    void prepareForDownloadedAsset(
            String assetId,
            String playbackSessionId,
            String analyticsBaseUrl,
            String cdnProvider,
            PrepareCallback callback
    );

    void release();

    List<EnigmaExternalAdMarker> getAdMarkers();
}
