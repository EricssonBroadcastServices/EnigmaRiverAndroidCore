package com.redbeemedia.enigma.core.epg.impl;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.epg.response.IEpgResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*package-protected*/ class EpgResponse implements IEpgResponse {
    private final List<IProgram> programs;
    private final long startUtcMillis;
    private final long endUtcMillis;

    public EpgResponse(List<IProgram> programs, long startUtcMillis, long endUtcMillis) {
        this.programs = Collections.unmodifiableList(programs);
        this.startUtcMillis = startUtcMillis;
        this.endUtcMillis = endUtcMillis;
    }

    @Override
    public long getStartUtcMillis() {
        return startUtcMillis;
    }

    @Override
    public long getEndUtcMillis() {
        return endUtcMillis;
    }

    @Override
    public IProgram getProgramAt(long utcMillis) {
        if(utcMillis < startUtcMillis || utcMillis > endUtcMillis) {
            return null;
        }
        List<IProgram> hits = new ArrayList<>();
        for(IProgram program : programs) {
            if(containsTime(program, utcMillis)) {
                hits.add(program);
            }
        }
        if(hits.isEmpty()) {
            return null;
        } else if(hits.size() == 1) {
            return hits.get(0);
        } else {
            return Collections.max(hits, (o1, o2) -> Long.compare(o1.getStartUtcMillis(), o2.getStartUtcMillis()));
        }
    }

    private boolean containsTime(IProgram program, long utcMillis) {
        return program.getStartUtcMillis() <= utcMillis && program.getEndUtcMillis() >= utcMillis;
    }

    @Override
    public List<IProgram> getPrograms() {
        return programs;
    }
}
