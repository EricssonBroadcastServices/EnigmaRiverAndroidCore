package com.redbeemedia.enigma.core.playable;

import java.net.URL;

public interface IPlayableHandler {
    void startUsingAssetId(String assetId);
    void startUsingUrl(URL url);
}
