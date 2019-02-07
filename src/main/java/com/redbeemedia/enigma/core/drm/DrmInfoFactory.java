package com.redbeemedia.enigma.core.drm;

public class DrmInfoFactory {
    public static IDrmInfo createWidevineDrmInfo(String licenseUrl, String playToken) {
        return new DrmInfo(licenseUrl, playToken);
    }
}
