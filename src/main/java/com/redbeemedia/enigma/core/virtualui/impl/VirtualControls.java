package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.playbacksession.BasePlaybackSessionListener;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSessionListener;
import com.redbeemedia.enigma.core.player.EnigmaPlayerState;
import com.redbeemedia.enigma.core.player.IEnigmaPlayer;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;
import com.redbeemedia.enigma.core.util.OpenContainer;
import com.redbeemedia.enigma.core.util.OpenContainerUtil;
import com.redbeemedia.enigma.core.virtualui.IVirtualButton;
import com.redbeemedia.enigma.core.virtualui.IVirtualControls;
import com.redbeemedia.enigma.core.virtualui.IVirtualControlsSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class VirtualControls implements IVirtualControls {
    private final IVirtualButton rewind;
    private final IVirtualButton fastForward;
    private final IVirtualButton play;
    private final IVirtualButton pause;
    private final IVirtualButton goToLive;
    private final IVirtualButton nextProgram;
    private final IVirtualButton previousProgram;
    private final IVirtualButton restart;
    private final IVirtualButton seekBar;

    /**
     * Will ensure we have single instance of IVirtualControls
     *
     * @param enigmaPlayer
     * @param settings
     * @return
     */
    public static IVirtualControls create(IEnigmaPlayer enigmaPlayer, IVirtualControlsSettings settings) {
        return new VirtualControls(enigmaPlayer, settings);
    }

    private VirtualControls(IEnigmaPlayer enigmaPlayer, IVirtualControlsSettings settings) {
        VirtualButtonContainer buttonContainer = new VirtualButtonContainer(enigmaPlayer, settings);
        this.rewind = new RewindButton(buttonContainer);
        this.fastForward = new FastForwardButton(buttonContainer);
        this.play = new PlayButton(buttonContainer);
        this.seekBar = new SeekBar(buttonContainer);
        this.pause = new PauseButton(buttonContainer);
        this.goToLive = new GoToLiveButton(buttonContainer);
        this.nextProgram = new JumpProgramButton(buttonContainer, false);
        this.previousProgram = new JumpProgramButton(buttonContainer, true);
        this.restart = new RestartButton(buttonContainer);

        buttonContainer.refreshButtons();
    }

    @Override
    public IVirtualButton getRewind() {
        return rewind;
    }

    @Override
    public IVirtualButton getFastForward() {
        return fastForward;
    }

    @Override
    public IVirtualButton getPlay() {
        return play;
    }

    @Override
    public IVirtualButton getPause() {
        return pause;
    }

    @Override
    public IVirtualButton getGoToLive() {
        return goToLive;
    }

    @Override
    public IVirtualButton getNextProgram() {
        return nextProgram;
    }

    @Override
    public IVirtualButton getPreviousProgram() {
        return previousProgram;
    }

    @Override
    public IVirtualButton getSeekBar() {
        return seekBar;
    }

    @Override
    public IVirtualButton getRestart() {
        return restart;
    }

    public void setEnabled(IVirtualButton button, boolean newEnabled) {
        AbstractVirtualButtonImpl abstractVirtualButton = (AbstractVirtualButtonImpl) button;
        abstractVirtualButton.setEnabled(newEnabled);
    }

    private static class VirtualButtonContainer implements IVirtualButtonContainer {
        private final IEnigmaPlayer enigmaPlayer;
        private final IVirtualControlsSettings settings;
        private final OpenContainer<EnigmaPlayerState> enigmaPlayerState;
        private final OpenContainer<IContractRestrictions> contractRestrictions = new OpenContainer<>(null);
        private final OpenContainer<IPlaybackSession> playbackSession = new OpenContainer<>(null);

        private final List<AbstractVirtualButtonImpl> virtualButtons = new ArrayList<>();

        public VirtualButtonContainer(IEnigmaPlayer enigmaPlayer, IVirtualControlsSettings settings) {
            Objects.requireNonNull(enigmaPlayer, "enigmaPlayer was null");
            Objects.requireNonNull(settings, "settings was null");

            this.enigmaPlayer = enigmaPlayer;
            this.settings = settings;
            this.enigmaPlayerState = new OpenContainer<>(enigmaPlayer.getState());
            enigmaPlayer.addListener(new BaseEnigmaPlayerListener() {
                private final IPlaybackSessionListener playbackSessionListener = new BasePlaybackSessionListener() {
                    @Override
                    public void onContractRestrictionsChanged(IContractRestrictions oldContractRestrictions, IContractRestrictions newContractRestrictions) {
                        OpenContainerUtil.setValueSynchronized(contractRestrictions, newContractRestrictions, (oldValue, newValue) -> refreshButtons());
                    }

                    @Override
                    public void onPlayingFromLiveChanged(boolean live) {
                        refreshButtons();
                    }
                };

                @Override
                public void onStateChanged(EnigmaPlayerState from, EnigmaPlayerState to) {
                    OpenContainerUtil.setValueSynchronized(enigmaPlayerState, to, (oldValue, newValue) -> refreshButtons());
                }

                @Override
                public void onPlaybackSessionChanged(IPlaybackSession from, IPlaybackSession to) {
                    if (from != null) {
                        from.removeListener(playbackSessionListener);
                    }
                    if (to != null) {
                        OpenContainerUtil.setValueSynchronized(contractRestrictions, to.getContractRestrictions(), (oldValue, newValue) -> refreshButtons());
                        to.addListener(playbackSessionListener);
                    }
                    OpenContainerUtil.setValueSynchronized(playbackSession, to, (oldValue, newValue) -> refreshButtons());
                }
            });
        }

        private void refreshButtons() {
            for (AbstractVirtualButtonImpl virtualButton : virtualButtons) {
                virtualButton.refresh();
            }
        }

        @Override
        public void addButton(AbstractVirtualButtonImpl virtualButton) {
            virtualButtons.add(virtualButton);
        }

        @Override
        public IEnigmaPlayerControls getPlayerControls() {
            return enigmaPlayer.getControls();
        }

        @Override
        public EnigmaPlayerState getPlayerState() {
            return OpenContainerUtil.getValueSynchronized(enigmaPlayerState);
        }

        @Override
        public IContractRestrictions getContractRestrictions() {
            return OpenContainerUtil.getValueSynchronized(contractRestrictions);
        }

        @Override
        public IEnigmaPlayer getEnigmaPlayer() {
            return enigmaPlayer;
        }

        @Override
        public IVirtualControlsSettings getSettings() {
            return settings;
        }

        @Override
        public IPlaybackSession getPlaybackSession() {
            return OpenContainerUtil.getValueSynchronized(playbackSession);
        }
    }
}
