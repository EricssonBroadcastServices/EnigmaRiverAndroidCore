// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player.timeline;

import android.os.Handler;

/**
 * Represents the timeline associated with an ongoing playback.
 */
public interface ITimeline {

    /**
     * Adds a ITimelineListener to receive updates of the values of the timeline.
     * @param listener
     */
    void addListener(ITimelineListener listener);

    /**
     * Adds a ITimelineListener to receive updates of the values of the timeline.
     * The `handler` will be used to specify on what thread the callback will be called.
     * @param listener
     */
    void addListener(ITimelineListener listener, Handler handler);

    /**
     * Removes a ITimelineListener from the delegate collection.
     * @param listener
     */
    void removeListener(ITimelineListener listener);

    /**
     * Returns the current playback position in the current content window (or ad)
     * @return the playback position or null
     */
    ITimelinePosition getCurrentPosition();

    /**
     * @return The start position of the stream or null.
     */
    ITimelinePosition getCurrentStartBound();

    /**
     * @return Returns the end of the stream or null.
     */
    ITimelinePosition getCurrentEndBound();

    /**
     * Returns the position considered to be the "live" position during linear playback.
     * @return a ITimelinePosition if found or null if the playback is not linear or if an error occured.
     */
    ITimelinePosition getLivePosition();

    /**
     * If false, the graphical timeline is expected to be hidden.
     * @return true if the timeline is expected to be visible.
     */
    boolean getVisibility();

    /**
     * @returns Metadata which includes eventStreams data
     */
    void onDashMetadata(EnigmaMetadata metadata);

    /**
     * @returns Metadata which includes eventStreams data
     */
    void onHlsMetadata(EnigmaHlsMediaPlaylist metadata);
}
