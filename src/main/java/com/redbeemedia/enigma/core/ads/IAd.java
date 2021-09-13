package com.redbeemedia.enigma.core.ads;

import androidx.annotation.Nullable;

/** Represents an event. */
public interface IAd {
    /** The id of an ad. */
    @Nullable String getId();
    /** The title of the ad .*/
    @Nullable String getTitle();
    /** The absolute start time of an ad (in ms).*/
    long getStartTime();
    /** The ad duration (in ms). */
    long getDuration();

}
