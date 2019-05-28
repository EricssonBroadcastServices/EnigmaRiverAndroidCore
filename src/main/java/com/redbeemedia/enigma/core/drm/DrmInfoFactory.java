package com.redbeemedia.enigma.core.drm;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public class DrmInfoFactory {
    public static IDrmInfo createWidevineDrmInfo(String licenseUrl, String playToken, String requestId) {
        return new DrmInfo(licenseUrl, playToken, requestId);
    }
}
