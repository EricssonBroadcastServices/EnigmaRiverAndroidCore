// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.player.timeline.ITimeline;
import com.redbeemedia.enigma.core.player.timeline.ITimelineListener;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.time.Duration;

import java.util.List;

/** A "transposed" version of the timeline where the length of the sum of the ad breaks are removed. */
public interface IAdIncludedTimeline extends ITimeline, ITimelineListener {

    /** Returns the duration of all completed ads prior to current position. */
    Duration getPastAdDuration();

    /** Returns the current ad break (or null if not playing an ad break). The start time of the ad break is transposed.*/
    @Nullable AdBreak getCurrentAdBreak();

    /** If `active`, the timeline will take ad information into account. */
    void setIsActive(boolean isActive);

    /** Returns true if `setIsActive` has been invoked and if ads has been detected. */
    boolean isActive();

    /** Returns the transposed ad breaks for this timeline as points in the timeline. */
    @Nullable List<ITimelinePosition> getAdBreaksPositions();

    /** Returns the ad breaks for this timeline  */
    @Nullable List<AdBreak> getAdBreaks();
}
