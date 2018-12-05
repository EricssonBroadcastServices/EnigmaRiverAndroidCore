package com.redbeemedia.enigma.core.entitlement;

public class Format {

    private String format;
    private String drmCertificateUrl;
    private String drmLicenseServerUrl;
    private String mediaLocator;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getDrmCertificateUrl() {
        return drmCertificateUrl;
    }

    public void setDrmCertificateUrl(String drmCertificateUrl) {
        this.drmCertificateUrl = drmCertificateUrl;
    }

    public String getDrmLicenseServerUrl() {
        return drmLicenseServerUrl;
    }

    public void setDrmLicenseServerUrl(String drmLicenseServerUrl) {
        this.drmLicenseServerUrl = drmLicenseServerUrl;
    }

    public String getMediaLocator() {
        return mediaLocator;
    }

    public void setMediaLocator(String mediaLocator) {
        this.mediaLocator = mediaLocator;
    }
}