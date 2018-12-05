package com.redbeemedia.enigma.core.player;

public interface IDrmPlayerImplementation {
    void startPlaybackWithDrm(String url, String licenceUrl, String[] keyRequestPropertiesArray);
}
