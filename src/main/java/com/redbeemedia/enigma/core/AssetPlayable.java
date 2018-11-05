package com.redbeemedia.enigma.core;

public class AssetPlayable implements IPlayable {
    private String assetId;

    public AssetPlayable(String assetId) {
        this.assetId = assetId;
    }

    @Override
    public void useWith(IPlayableHandler playableHandler) {
        playableHandler.startUsingAssetId(assetId);
    }
}
