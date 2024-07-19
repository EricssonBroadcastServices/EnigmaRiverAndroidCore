// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import androidx.annotation.Nullable;

import java.util.Map;

/**
 * Represent a single ad entry in a VAST manifest.
 * Capable of determine which entry URLs corresponding to a time factor.
 */
public class VastAdEntry implements IAd {

    /** Used to determine the factor relative to the start / end bound of an ad where it's considered to be playing. */
    static final float AD_GRACE_FACTOR = 0.2f;

    /** The maximum grate time allowed in MS. The total ad grace time is inversely proportional to the duration of the ad. */
    static final long AD_GRACE_MAXIMUM_MS = (long)(12000f * AD_GRACE_FACTOR);

    private final Map<AdEventType, VastImpression> impressions;

    private final String id;
    private final long duration;
    private final long startTime;
    private final String title;
    private long currentTime;
    private final VideoClicks videoClicks;

    /**
     * Set up the VastAdEntry model.
     * @param id An optional id for the ad.
     * @param title An optional title for the event.
     * @param startTime The absolute time when an ad is scheduled to start (in ms).
     * @param duration The duration of an ad (in ms).
     * @param impressions The container for all event types and their corresponding URLs.
     */
    VastAdEntry(@Nullable String id, @Nullable String title, long startTime, long duration, @Nullable Map<AdEventType, VastImpression> impressions, @Nullable VideoClicks videoClicks) {
        this.id = id;
        this.title = title;
        this.duration = duration;
        this.impressions = impressions;
        this.startTime = startTime;
        this.videoClicks = videoClicks;
    }


    public boolean isSent() {
        for(VastImpression impression: impressions.values()) {
            if(!impression.isSent()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the <code>VastImpression</code> corresponding to the relative position within the ad's duration.
     * I.e. 0.1 will return `Start` and 0.51 will return `MidPoint`.
     * @param factor A factor (typically 0.0 - 1.0) representing the relative position in an ad.
     * @return A <code>VastImpression</code> if `factor` is considered to be within the bounds of the ad.
     */
    @Nullable private VastImpression getEntrySet(float factor) {
        AdEventType type = getEventType(factor);
        if(type != null) {
            return getEntrySet(type);
        }
        return null;
    }

    /**
     * Set the current time stamp (in ms) for the ad.
     * This is required in order to fetch a VastImpression.
     * @param currentTime an absolute time (in ms).
     */
    void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    /**
     * Calculate the <code>VastImpression</code> for this entry set. This is done by
     * comparing `currentTime` with the start time of the ad and the duration of the
     * contained <code>VastAdEntry</code> objects.</br>
     * In order to set the current time, use <code>VastAdEntry#setCurrentTime(long)</code>
     * @return The <code>VastImpression</code> played at `currentTime` or `null` if no ad is playing at `currentTime`.
     */
    @Nullable public VastImpression getImpression() {
        return getImpression(currentTime);
    }

    /** Calculates and returns an event type for the specified factor (if within -0.1 and 1.2, the ad is considered to be playing). */
    @Nullable AdEventType getEventType(float factor) {
        return factor < 0.25  && factor >= -AD_GRACE_FACTOR ? AdEventType.Start :
               factor >= 0.25 && factor < 0.50 ? AdEventType.FirstQuartile :
               factor >= 0.5 && factor < 0.75 ? AdEventType.MidPoint :
               factor >= 0.75 && factor < 0.95 ? AdEventType.ThirdQuartile :
               factor >= 0.95 && factor < 1.0 + AD_GRACE_FACTOR ? AdEventType.Complete:
               null;
    }

    /** Return the impressions for the specified eventType (or null if no impression for this eventType exists). */
    @Nullable VastImpression getEntrySet(AdEventType eventType) {
        if(impressions != null && impressions.containsKey(eventType)) {
            return impressions.get(eventType);
        }
        return null;
    }

    /** Returns the AdEventType corresponding to currentTime or null if no ad event existed. */
    @Nullable AdEventType getEventType(long currentTime) {
        float factor = getFactor(currentTime);
        return getEventType(factor);
    }

    /** Returns the relative factor of the event. */
    float getFactor(long currentTime) {
        if(duration == 0) { return -1; }
        long endTime = startTime + duration;
        return 1.0f - ((float)(endTime - currentTime)) / ((float)duration);
    }

    @Nullable VastImpression getImpression(long currentTime) {
        float factor = getFactor(currentTime);
        return getEntrySet(factor);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof VastAdEntry &&
               ((VastAdEntry)o).getId().equals(id);
    }

    @Override
    @Nullable public String getId() { return id; }

    @Override
    @Nullable public String getTitle() { return title; }

    @Override
    public long getStartTime() { return startTime; }

    @Override
    public long getDuration() { return duration; }

    public VideoClicks getVideoClicks() {
        return videoClicks;
    }
}
