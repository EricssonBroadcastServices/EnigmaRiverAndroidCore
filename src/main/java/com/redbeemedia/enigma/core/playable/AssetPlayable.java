package com.redbeemedia.enigma.core.playable;

import android.text.TextUtils;

public class AssetPlayable implements IPlayable {

    private final String assetId;

    public AssetPlayable(final String assetId) {
        this.assetId = assetId;
    }

    @Override
    public void useWith(final IPlayableHandler playableHandler) {
        if (!TextUtils.isEmpty(assetId)) {
            playableHandler.startUsingAssetId(assetId);
        }
    }
}
