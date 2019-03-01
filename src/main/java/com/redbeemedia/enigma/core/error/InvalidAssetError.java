package com.redbeemedia.enigma.core.error;

import java.lang.String;


public class InvalidAssetError extends AssetPlayRequestError {
    private String assetId;

    public InvalidAssetError(String assetId) {
        this(assetId, null);
    }

    public InvalidAssetError(String assetId, Error cause) {
        super(getAssetMessage(assetId), cause);
        this.assetId = assetId;
    }


    @Override
    public int getErrorCode() {
        return ErrorCode.INVALID_ASSET;
    }

    public String getAssetId() {
        return this.assetId;
    }

    public static String getAssetMessage(String assetId) {
        return "Could not find asset with id "+assetId;
    }
}