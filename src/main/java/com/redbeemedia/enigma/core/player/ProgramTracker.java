// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import android.os.Handler;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;

import java.util.ArrayList;
import java.util.List;

/*package-protected*/ class ProgramTracker {
    private final OpenContainer<IProgram> currentProgram = new OpenContainer<>(null);
    private final OpenContainer<IProgram> currentProgramForEntitlementCheck = new OpenContainer<>(null);
    private final OpenContainer<IStreamPrograms> currentStreamPrograms = new OpenContainer<>(null);
    private final List<IProgramChangedListener> listeners = new ArrayList<>();
    private final List<IProgramChangeCheckEntitlementListener> entitlementListeners = new ArrayList<>();

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
                    changeProgram(streamPrograms != null ? streamPrograms.getProgram() : null);
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
                newCurrentProgram = currentStreamPrograms.value.getProgram();
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

    public void onOffsetChangedCheckEntitlement() {
        IProgram newCurrentProgram = null;
        synchronized (currentStreamPrograms) {
            if(currentStreamPrograms.value != null) {
                newCurrentProgram = currentStreamPrograms.value.getProgramForEntitlementCheck();
            }
        }
        boolean checkProgramEntitlement = false;
        synchronized (currentProgramForEntitlementCheck) {
            if (currentProgramForEntitlementCheck.value != newCurrentProgram) {
                if (newCurrentProgram == null ||
                        currentProgramForEntitlementCheck.value == null ||
                        currentProgramForEntitlementCheck.value.getStartUtcMillis() <= newCurrentProgram.getStartUtcMillis()) {
                            checkProgramEntitlement = true;
                }
            }
        }
        if (checkProgramEntitlement) {
            boolean waitForEntitlement = (currentProgramForEntitlementCheck.value != null);
            currentProgramForEntitlementCheck.value = newCurrentProgram;
            if (waitForEntitlement) {
                int randomNumberBetween2Mins = ((int) (Math.random() * 120)) * 1000;
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    checkEntitlement(currentProgramForEntitlementCheck.value);
                }, randomNumberBetween2Mins);
            } else {
                checkEntitlement(currentProgramForEntitlementCheck.value);
            }
        }
    }

    public IProgram getCurrentProgram() {
        return OpenContainerUtil.getValueSynchronized(currentProgram);
    }

    private void onProgramChanged(IProgram oldProgram, IProgram newProgram) {
        for(IProgramChangedListener listener : listeners) {
            listener.onProgramChanged(oldProgram, newProgram);
        }
    }

    private void checkEntitlement(IProgram newProgram) {
        for (IProgramChangeCheckEntitlementListener listener : entitlementListeners) {
            listener.checkEntitlement(newProgram);
        }
    }

    public ProgramTracker addListener(IProgramChangedListener listener) {
        this.listeners.add(listener);
        return this;
    }

    public ProgramTracker addListener(IProgramChangeCheckEntitlementListener listener) {
        this.entitlementListeners.add(listener);
        return this;
    }

    public interface IProgramChangedListener {
        void onProgramChanged(IProgram oldProgram, IProgram newProgram);
    }

    public interface IProgramChangeCheckEntitlementListener {
        void checkEntitlement(IProgram newProgram);
    }
}


