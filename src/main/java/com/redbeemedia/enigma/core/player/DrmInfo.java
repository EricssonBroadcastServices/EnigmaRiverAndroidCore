package com.redbeemedia.enigma.core.player;

public class DrmInfo implements IDrmInfo {
    private String licenseUrl;
    private String[] drmKeyRequestPropertiesArray;

    public DrmInfo(String licenseUrl, String playToken) {
        this.licenseUrl = licenseUrl;
        this.drmKeyRequestPropertiesArray = createDrmKeyRequestPropertiesArray(playToken);
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public String[] getDrmKeyRequestPropertiesArray() {
        return drmKeyRequestPropertiesArray;
    }

    private String[] createDrmKeyRequestPropertiesArray(String playToken) {
        return new String[]{"Authorization", "Bearer " + playToken};
    }
}