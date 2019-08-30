package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.epg.IEpg;
import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.error.EnigmaErrorException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*package-protected*/ class StreamPrograms implements IStreamPrograms {
    private final StreamInfo streamInfo;
    private final OpenContainer<List<IProgram>> programs = new OpenContainer<>(null);

    public StreamPrograms(StreamInfo streamInfo) {
        this.streamInfo = streamInfo;

        List<IProgram> modifiablePrograms = new ArrayList<>();

        IEpg epg = EnigmaRiverContext.getEpg();
        epg.getPrograms(streamInfo.getChannelId(), streamInfo.getStartUtcSeconds() * 1000L, streamInfo.getEndUtcSeconds() * 1000L, new IEpg.IProgramListRequestResultHandler() {
            @Override
            public void onList(List<IProgram> programs) {
                synchronized (programs) {
                    modifiablePrograms.clear();
                    modifiablePrograms.addAll(programs);

                    Collections.sort(modifiablePrograms, (o1, o2) -> Long.compare(o1.getStartUtcMillis(), o2.getStartUtcMillis()));
                }
            }

            @Override
            public void onError(Error error) {
                throw new EnigmaErrorException(error);
            }
        });
        synchronized (programs) {
            programs.value = Collections.unmodifiableList(modifiablePrograms);
        }
    }

    @Override
    public IProgram getProgramAtOffset(long offset) {
        long utcMillis = streamInfo.getStartUtcSeconds()*1000L+offset;
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
