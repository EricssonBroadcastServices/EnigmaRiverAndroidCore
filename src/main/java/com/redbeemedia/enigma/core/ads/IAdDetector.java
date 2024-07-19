// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.time.Duration;

/**
 * Responsible for detecting and reporting ad information retrieved
 * from a remote endpoint.
 */
public interface IAdDetector {

    /** Called whenever an ad is played or has finished. */
    interface IAdStateListener {
        /**
         * Called whenever ads stops or starts playing.
         * @param adsDetector the ad detector being used.
         * @param currentAdd the current <code>IAd</code> being played or null if no ad is actively playing.
         * @param eventType the current <code>AdEventType</code> representing the absolute position <i>within</i> the played ad.
         */
        void adStateChanged(IAdDetector adsDetector, @Nullable IAd currentAdd, AdEventType eventType);
    }

    /** Default <code>IAdStateListener</code> implementation. */
    abstract class AdStateListener implements IAdStateListener {
        @Override
        public void adStateChanged(IAdDetector adsDetector, @Nullable IAd currentAdd, AdEventType eventType) { }
    }

    /** Returns the timeline that handles the ad breaks. */
    IAdIncludedTimeline getTimeline();

    /**
     * @return The factory used to create <code>IAdResourceLoader</code> requests.
     */
    IAdResourceLoaderFactory getFactory();

    /** If true, the IAdDetector should listen to changes in player position. This value is set once before a stream playback has started. */
    void setEnabled(boolean isEnabled);

    /** @return true if an ad is playing, otherwise false. */
    boolean isAdPlaying();

    /** @return current VastAdEntry */
    VastAdEntry getCurrentAdEntry();

    /** @return true if an ad is playing, otherwise false. */
    void setAdPlaying(boolean adPlaying);

    /** Add a listener to changes in ads events. */
    void addListener(IAdStateListener listener);

    /**
     * Starts the ad detection.
     * This will fetch the metadata from the resource, parse it and call
     * impression links found in the metadata if the playback position is
     * within the bounds of an ad.
     * @param resourceLoader Responsible for fetching the metadata resources.
     * @param startTime The absolute start time in the stream of the first ad. This is typically 0 for a VOD asset, but a relative position if it's a LIVE stream.
     */
    void update(IAdResourceLoader resourceLoader, long startTime);

    /**
     * to convert to timeline object
     * @param time
     * @return
     */
    ITimelinePosition convertToTimeline(long time);

    /**
     * will return is ssai is enabled or not
     * @return
     */
    boolean isSsaiEnabled();

    /**
     * will return livedelay value
     * @return
     */
    Duration getLiveDelay();

    /**
     * will return livedelay value
     * @return
     */
    void setLiveDelay(Duration liveDelay);

    /**
     * send video click impression
     */
    void sendVideoAdClickImpression();
}
