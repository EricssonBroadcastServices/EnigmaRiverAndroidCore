package com.redbeemedia.enigma.core.epg.response;

import com.redbeemedia.enigma.core.epg.IProgram;

import java.util.List;

public interface IEpgResponse {
    long getStartUtcMillis();
    long getEndUtcMillis();
    IProgram getProgramAt(long utcMillis);
    List<IProgram> getPrograms();
}
