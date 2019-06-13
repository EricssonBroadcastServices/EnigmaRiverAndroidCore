package com.redbeemedia.enigma.core.playable;

import java.net.URL;

/**
 * <h3>NOTE</h3>
 * <p>Implementing or extending this interface is not part of the public API.</p>
 */
public interface IPlayableHandler {
    void startUsingAssetId(String assetId);
    void startUsingUrl(URL url);
}
