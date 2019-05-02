package com.redbeemedia.enigma.core.playable;

public class MockPlayable implements IPlayable {
    private String assetID;

    public MockPlayable(String assetID) {
        this.assetID = assetID;
    }

    @Override
    public void useWith(IPlayableHandler playableHandler) {
        playableHandler.startUsingAssetId(assetID);
    }
}
