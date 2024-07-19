// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.error;

import com.redbeemedia.enigma.core.session.ISession;
import java.lang.String;


/**
 * The maximum number of downloads of this asset has been reached
 * for the user owning theISession used for the request.
 */
public class MaxDownloadCountLimitReachedError extends DownloadError {
    private String assetId;

    public MaxDownloadCountLimitReachedError(String assetId) {
        this(assetId, null, null);
    }

    public MaxDownloadCountLimitReachedError(String assetId, EnigmaError cause) {
        this(assetId, null, cause);
    }

    public MaxDownloadCountLimitReachedError(String assetId, String message) {
        this(assetId, message, null);
    }

    public MaxDownloadCountLimitReachedError(String assetId, String message, EnigmaError cause) {
        super(message, cause);
        this.assetId = assetId;
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.MAX_DOWNLOAD_COUNT_LIMIT_REACHED;
    }

    public String getAssetId() {
        return this.assetId;
    }
}
