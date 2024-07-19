// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.InternalError;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestriction;
import com.redbeemedia.enigma.core.time.Duration;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public class ControlLogic {

    public static IValidationResults<Long> validateProgramJump(boolean jumpBackwards,
                                                         IPlaybackSession playbackSession) {
        if(playbackSession == null) {
            return new RejectedResult<>(RejectReason.inapplicable("No active playback session"));
        }
        IInternalPlaybackSession internalPlaybackSession = (IInternalPlaybackSession) playbackSession;

        IStreamPrograms streamPrograms = internalPlaybackSession.getStreamPrograms();
        if(streamPrograms == null) {
            return new RejectedResult<>(RejectReason.inapplicable("The current stream has no programs"));
        }

        Duration currentPlaybackOffset = internalPlaybackSession.getPlaybackSessionInfo().getCurrentPlaybackOffset();
        long offset = currentPlaybackOffset.inWholeUnits(Duration.Unit.MILLISECONDS);
        Long neighbouringStart = streamPrograms.getNeighbouringSectionStartOffset(offset, jumpBackwards);
        if(neighbouringStart == null) {
            return new RejectedResult<>(RejectReason.inapplicable(jumpBackwards ? "First section in stream" : "Last section in stream"));
        }

        return new SuccessResult<>(neighbouringStart);
    }

    public static IValidationResults<IPlayerImplementationControls.ISeekPosition> validateSeek(IEnigmaPlayerControls.StreamPosition streamPosition, IPlaybackSession playbackSession) {
        if(streamPosition == IEnigmaPlayerControls.StreamPosition.START) {
            if(seekAllowed(playbackSession)) {
                return new SuccessResult<>(IPlayerImplementationControls.ISeekPosition.TIMELINE_START);
            } else {
                return new RejectedResult<>(RejectReason.contractRestriction("Seek not allowed"));
            }
        } else if(streamPosition == IEnigmaPlayerControls.StreamPosition.LIVE_EDGE) {
            if(seekToLiveAllowed(playbackSession)) {
                return new SuccessResult<>(IPlayerImplementationControls.ISeekPosition.LIVE_EDGE);
            } else if(seekAllowed(playbackSession)) {
                return new RejectedResult<>(RejectReason.inapplicable("Seek to live not allowed"));
            } else {
                return new RejectedResult<>(RejectReason.contractRestriction("Seek not allowed"));
            }
        } else {
            return new RejectedResult<>(RejectReason.illegal("Unknown "+ IEnigmaPlayerControls.StreamPosition.class.getSimpleName()+" \""+streamPosition+"\""));
        }
    }

    public static IValidationResults<Void> validateSeek(boolean seekForward, boolean seekBackward, IPlaybackSession playbackSession) {
        if(playbackSession == null) {
            return new RejectedResult<>(RejectReason.incorrectState("No playback session"));
        }
        if(seekForward && !fastForwardEnabled(playbackSession)) {
            return new RejectedResult<>(RejectReason.contractRestriction("Fast-forward not enabled"));
        } else if(seekBackward && !rewindEnabled(playbackSession)) {
            return new RejectedResult<>(RejectReason.contractRestriction("Rewind not enabled"));
        }
        if(seekAllowed(playbackSession)) {
            return new SuccessResult<>(null);
        } else {
            return new RejectedResult<>(RejectReason.contractRestriction("Seek not allowed"));
        }
    }

    public static IValidationResults<Void> validatePause(EnigmaPlayerState currentState, IPlaybackSession playbackSession) {
        if(playbackSession == null) {
            return new ErrorResult<>(new InternalError("No PlaybackSession"));
        } else {
            if(!getContractRestrictionValue(playbackSession, ContractRestriction.TIMESHIFT_ENABLED, true)) {
                return new RejectedResult<>(RejectReason.contractRestriction("Timeshift not enabled"));
            }
        }
        if(currentState != EnigmaPlayerState.PLAYING && currentState != EnigmaPlayerState.PAUSED && currentState != EnigmaPlayerState.BUFFERING) {
            return new RejectedResult<>(RejectReason.incorrectState("Player is "+currentState));
        } else {
            return new SuccessResult<>(null);
        }
    }

    public static IValidationResults<Void> validateStart(EnigmaPlayerState currentState, IPlaybackSession playbackSession, boolean hasPlaybackSessionSeed) {
        if(playbackSession != null) {
            if(currentState == EnigmaPlayerState.IDLE) {
                return new RejectedResult<>(RejectReason.incorrectState("Player is IDLE"));
            } else if(currentState == EnigmaPlayerState.LOADING) {
                return new RejectedResult<>(RejectReason.incorrectState("Player is LOADING"));
            } else if(currentState == EnigmaPlayerState.BUFFERING) {
                return new RejectedResult<>(RejectReason.incorrectState("Player is BUFFERING"));
            } else {
                return new SuccessResult<>(null);
            }
        } else {
            if(hasPlaybackSessionSeed) {
                return new SuccessResult<>(null);
            } else {
                return new RejectedResult<>(RejectReason.inapplicable("No active playback session"));
            }
        }
    }

    private static boolean fastForwardEnabled(IPlaybackSession playbackSession) {
        boolean ffEnabled = getContractRestrictionValue(playbackSession, ContractRestriction.FASTFORWARD_ENABLED,true);
        boolean timeshiftEnabled = getContractRestrictionValue(playbackSession, ContractRestriction.TIMESHIFT_ENABLED,true);
        return ffEnabled && timeshiftEnabled;
    }

    private static boolean rewindEnabled(IPlaybackSession playbackSession) {
        boolean rwEnabled = getContractRestrictionValue(playbackSession, ContractRestriction.REWIND_ENABLED,true);
        boolean timeshiftEnabled = getContractRestrictionValue(playbackSession, ContractRestriction.TIMESHIFT_ENABLED,true);
        return rwEnabled && timeshiftEnabled;
    }

    private static boolean seekAllowed(IPlaybackSession playbackSession) {
        return playbackSession != null && playbackSession.isSeekAllowed();
    }

    private static boolean seekToLiveAllowed(IPlaybackSession playbackSession) {
        return playbackSession != null && playbackSession.isSeekToLiveAllowed();
    }


    private static <T> T getContractRestrictionValue(IPlaybackSession playbackSession, IContractRestriction<T> contractRestriction, T fallback) {
        if(playbackSession != null) {
            return playbackSession.getContractRestrictions().getValue(contractRestriction, fallback);
        } else {
            return fallback;
        }
    }

    public interface IValidationResults<T> {
        boolean isSuccess();
        T getRelevantData();
    }

    public interface IFailedValidationResults<T> extends IValidationResults<T> {
        void triggerCallback(IPlayerImplementationControlResultHandler controlResultHandler);
    }

    private static class SuccessResult<T> implements IValidationResults<T> {
        private final T relevantData;

        public SuccessResult(T relevantData) {
            this.relevantData = relevantData;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public T getRelevantData() {
            return relevantData;
        }
    }

    private static abstract class ValidationFailed<T> implements IFailedValidationResults<T> {
        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public T getRelevantData() {
            throw new UnsupportedOperationException();
        }
    }

    private static class RejectedResult<T> extends ValidationFailed<T> {
        private final IControlResultHandler.IRejectReason rejectReason;

        public RejectedResult(IControlResultHandler.IRejectReason rejectReason) {
            this.rejectReason = rejectReason;
        }

        @Override
        public void triggerCallback(IPlayerImplementationControlResultHandler controlResultHandler) {
            controlResultHandler.onRejected(rejectReason);
        }
    }

    private static class CancelledResult<T> extends ValidationFailed<T> {
        @Override
        public void triggerCallback(IPlayerImplementationControlResultHandler controlResultHandler) {
            controlResultHandler.onCancelled();
        }
    }

    private static class ErrorResult<T> extends ValidationFailed<T> {
        private final EnigmaError error;

        public ErrorResult(EnigmaError error) {
            this.error = error;
        }

        @Override
        public void triggerCallback(IPlayerImplementationControlResultHandler controlResultHandler) {
            controlResultHandler.onError(error);
        }
    }
}
