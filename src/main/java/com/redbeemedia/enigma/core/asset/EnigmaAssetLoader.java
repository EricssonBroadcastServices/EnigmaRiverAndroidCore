package com.redbeemedia.enigma.core.asset;

import com.redbeemedia.enigma.core.IPlayable;
import com.redbeemedia.enigma.core.UrlPlayable;

import java.net.MalformedURLException;
import java.net.URL;

public class EnigmaAssetLoader {
    private String exposureEndpoint;

    public EnigmaAssetLoader(String exposureEndpoint) {
        this.exposureEndpoint = exposureEndpoint;
    }

    public IAsset load(String assetId) throws MalformedURLException {
        return new EnigmaAsset(exposureEndpoint+"?assetId="+assetId);
    }

    private static class EnigmaAsset implements IAsset {
        private IPlayable playable;

        public EnigmaAsset(String url) throws MalformedURLException {
            this.playable = new UrlPlayable(new URL(url));
        }

        @Override
        public IPlayable getPlayable() {
            return playable;
        }
    }
}
