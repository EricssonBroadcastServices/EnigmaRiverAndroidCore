package com.redbeemedia.enigma.core.epg;

import com.redbeemedia.enigma.core.time.Duration;

public interface IProgram {
    Duration getDuration();
    long getStartUtcMillis();
    long getEndUtcMillis();
}
