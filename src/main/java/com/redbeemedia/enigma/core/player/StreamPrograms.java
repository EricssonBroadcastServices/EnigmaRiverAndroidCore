package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.epg.response.IEpgResponse;
import com.redbeemedia.enigma.core.util.OpenContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*package-protected*/ class StreamPrograms implements IStreamPrograms {
    private final long startUtcMillis;
    private final OpenContainer<List<IProgram>> programs = new OpenContainer<>(null);

    public StreamPrograms(IEpgResponse epgResponse) {
        this.startUtcMillis = epgResponse.getStartUtcMillis();

        List<IProgram> modifiablePrograms = new ArrayList<>();
        modifiablePrograms.addAll(epgResponse.getPrograms());
        Collections.sort(modifiablePrograms, (o1, o2) -> Long.compare(o1.getStartUtcMillis(), o2.getStartUtcMillis()));

        synchronized (programs) {
            programs.value = Collections.unmodifiableList(modifiablePrograms);
        }
    }

    @Override
    public IProgram getProgramAtOffset(long offset) {
        long utcMillis = startUtcMillis + offset;
        synchronized (programs) {
            if(programs.value != null) {
                for(IProgram program : programs.value) {
                    if(program.getStartUtcMillis() <= utcMillis && program.getEndUtcMillis() > utcMillis) {
                        return program;
                    }
                }
            }
        }
        return null;
    }

    private IProgram getAtIndex(int index) {
        synchronized (programs) {
            if(programs.value != null) {
                if (index < 0 || index >= programs.value.size()) {
                    return null;
                } else {
                    return programs.value.get(index);
                }
            } else {
                return null;
            }
        }
    }

    @Override
    public IProgram getNext(IProgram program) {
        synchronized (programs) {
            if(programs.value != null) {
                int index = programs.value.indexOf(program);
                if(index != -1) {
                    return getAtIndex(index+1);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    @Override
    public IProgram getPrevious(IProgram program) {
        synchronized (programs.value) {
            if(programs.value != null) {
                int index = programs.value.indexOf(program);
                if(index != -1) {
                    return getAtIndex(index-1);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }
}
