package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.util.OpenContainer;

import java.util.ArrayList;
import java.util.List;

/*package-protected*/ class ProgramTracker {
    private final OpenContainer<IProgram> currentProgram = new OpenContainer<>(null);
    private final OpenContainer<IStreamPrograms> currentStreamPrograms = new OpenContainer<>(null);
    private final List<IProgramChangedListener> listeners = new ArrayList<>();

    public void init(IEnigmaPlayer enigmaPlayer) {
        enigmaPlayer.addListener(new BaseEnigmaPlayerListener() {
            @Override
            public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
                if(to != null) {
                    IInternalPlaybackSession playbackSession = ((IInternalPlaybackSession) to);
                    IStreamPrograms streamPrograms = playbackSession.getStreamPrograms();
                    synchronized (currentStreamPrograms) {
                        currentStreamPrograms.value = streamPrograms;
                    }
                    changeProgram(streamPrograms != null ? streamPrograms.getProgramAtOffset(0L) : null);
                } else {
                    synchronized (currentStreamPrograms) {
                        currentStreamPrograms.value = null;
                    }
                    changeProgram(null);
                }
            }
        });
    }

    private final void changeProgram(IProgram newProgram) {
        IProgram oldProgram;
        synchronized (currentProgram) {
            oldProgram = currentProgram.value;
            currentProgram.value = newProgram;
        }
        onProgramChanged(oldProgram, newProgram);
    }

    public void onOffsetChanged(long millis) {
        IProgram newCurrentProgram = null;
        synchronized (currentStreamPrograms) {
             if(currentStreamPrograms.value != null) {
                 newCurrentProgram = currentStreamPrograms.value.getProgramAtOffset(millis);
             }
        }
        boolean programChanged = false;
        synchronized (currentProgram) {
            if(currentProgram.value != newCurrentProgram) {
                programChanged = true;
            }
        }
        if(programChanged) {
            changeProgram(newCurrentProgram);
        }
    }

    private void onProgramChanged(IProgram oldProgram, IProgram newProgram) {
        for(IProgramChangedListener listener : listeners) {
            listener.onProgramChanged(oldProgram, newProgram);
        }
    }

    public ProgramTracker addListener(IProgramChangedListener listener) {
        this.listeners.add(listener);
        return this;
    }


    public interface IProgramChangedListener {
        void onProgramChanged(IProgram oldProgram, IProgram newProgram);
    }
}


