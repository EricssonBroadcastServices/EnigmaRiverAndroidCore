package com.redbeemedia.enigma.core.ads;

import java.net.URL;
import java.util.List;

/**
 * Represents a set of VideoClicks entries in SSAI
 */
public class VideoClicks {
    private final String clickThroughUrl;
    private final List<URL> clickTrackingUrls;

    public VideoClicks(String clickThroughUrl, List<URL> clickTrackingUrls) {
        this.clickThroughUrl = clickThroughUrl;
        this.clickTrackingUrls = clickTrackingUrls;
    }

    public String getClickThroughUrl() {
        return clickThroughUrl;
    }

    public List<URL> getClickTrackingUrls() {
        return clickTrackingUrls;
    }
}
