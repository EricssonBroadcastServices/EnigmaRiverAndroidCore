// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.time.Duration;

import java.util.List;

/** Represents a section of a timeline containing consecutive ad segments. */
public class AdBreak {

    /** The starting point of the first ad. */
    final ITimelinePosition start;

    /** The duration of the all the ads in the segment. */
    final Duration duration;

    /** The ads and their absolute positions (not transposed to match the timeline). */
    final List<VastAdEntry> ads;

    private volatile boolean adShown;

    AdBreak(ITimelinePosition start, Duration duration, List<VastAdEntry> ads) {
        this.start = start;
        this.duration = duration;
        this.ads = ads;
    }

    /** Returns true if all the ads in the ad break has been played to the end. */
    public boolean getIsFinished() {
        for(VastAdEntry ad : ads) {
            if(!ad.isSent()) { return false; }
        }
        return true;
    }

    public boolean isAdShown() {
        return adShown;
    }

    public void setAdShown(boolean adShown) {
        this.adShown = adShown;
    }

    public ITimelinePosition getStart() {
        return start;
    }

    public ITimelinePosition getEnd() {
        return start.add(duration);
    }

    public List<VastAdEntry> getAds() {
        return ads;
    }
}
