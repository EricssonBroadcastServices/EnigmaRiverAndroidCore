// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.MockContractRestrictions;
import com.redbeemedia.enigma.core.restriction.MockContractRestrictionsValueSource;
import com.redbeemedia.enigma.core.testutil.Counter;

import org.junit.Assert;
import org.junit.Test;

public class ControlLogicTest {
    @Test
    public void testValidateStart() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        {
            IInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(false);
            Assert.assertEquals(true, ControlLogic.validateStart(EnigmaPlayerState.PAUSED, playbackSession, false).isSuccess());
            Assert.assertEquals(true, ControlLogic.validateStart(EnigmaPlayerState.PLAYING, playbackSession, false).isSuccess());
            Assert.assertEquals(true, ControlLogic.validateStart(EnigmaPlayerState.LOADED, playbackSession, false).isSuccess());
        }

        {
            IInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(false);
            ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateStart(EnigmaPlayerState.LOADING, playbackSession, false);
            Assert.assertEquals(false,validationResults.isSuccess());

            final Counter callbackCalled = new Counter();
            ((ControlLogic.IFailedValidationResults) validationResults).triggerCallback(new BasePlayerImplementationControlResultHandler() {
                @Override
                public void onRejected(IControlResultHandler.IRejectReason rejectReason) {
                    Assert.assertEquals(IControlResultHandler.RejectReasonType.INCORRECT_STATE, rejectReason.getType());
                    callbackCalled.count();
                }
            });
            callbackCalled.assertOnce();
        }
        {
            IInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(false);
            ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateStart(EnigmaPlayerState.IDLE, playbackSession, false);
            Assert.assertEquals(false,validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.INCORRECT_STATE);
        }
        {
            ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateStart(EnigmaPlayerState.IDLE, null, false);
            Assert.assertEquals(false,validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.INAPPLICABLE_FOR_CURRENT_STREAM);
        }
        {
            ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateStart(EnigmaPlayerState.IDLE, null, true);
            Assert.assertEquals(true,validationResults.isSuccess());
        }
        {
            ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateStart(EnigmaPlayerState.PAUSED, null, false);
            Assert.assertEquals(false,validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.INAPPLICABLE_FOR_CURRENT_STREAM);
        }
        {
            ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateStart(EnigmaPlayerState.PAUSED, null, true);
            Assert.assertEquals(true,validationResults.isSuccess());
        }
        {
            IInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(false);
            ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateStart(EnigmaPlayerState.PAUSED, playbackSession, true);
            Assert.assertEquals(true,validationResults.isSuccess());
        }
    }

    @Test
    public void testValidatePause() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        {
            MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(false);
            Assert.assertEquals(true, ControlLogic.validatePause(EnigmaPlayerState.PLAYING, playbackSession).isSuccess());
        }
        {
            MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(false);
            ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validatePause(EnigmaPlayerState.IDLE, playbackSession);
            Assert.assertEquals(false, validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.INCORRECT_STATE);
        }
        {
            MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(true);
            playbackSession.setContractRestrictions(new MockContractRestrictions().
                                                    setValueSource(new MockContractRestrictionsValueSource()
                                                                   .put(ContractRestriction.TIMESHIFT_ENABLED, false)
                                                    )
            );
            ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validatePause(EnigmaPlayerState.PLAYING, playbackSession);
            Assert.assertEquals(false, validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.CONTRACT_RESTRICTION_LIMITATION);
        }
    }

    @Test
    public void testValidateSeekWithStreamPosition() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        {
            MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(true);
            IEnigmaPlayerControls.StreamPosition streamPosition = IEnigmaPlayerControls.StreamPosition.START;
            ControlLogic.IValidationResults<IPlayerImplementationControls.ISeekPosition> validationResults = ControlLogic.validateSeek(streamPosition, playbackSession);
            Assert.assertEquals(true, validationResults.isSuccess());
            Assert.assertEquals(IPlayerImplementationControls.ISeekPosition.TIMELINE_START, validationResults.getRelevantData());
        }
        {
            MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(true) {
                @Override
                public boolean isSeekAllowed() {
                    return false;
                }
            };
            IEnigmaPlayerControls.StreamPosition streamPosition = IEnigmaPlayerControls.StreamPosition.START;
            ControlLogic.IValidationResults<IPlayerImplementationControls.ISeekPosition> validationResults = ControlLogic.validateSeek(streamPosition, playbackSession);
            Assert.assertEquals(false, validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.CONTRACT_RESTRICTION_LIMITATION);
        }
        {
            MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(true);
            IEnigmaPlayerControls.StreamPosition streamPosition = IEnigmaPlayerControls.StreamPosition.LIVE_EDGE;
            ControlLogic.IValidationResults<IPlayerImplementationControls.ISeekPosition> validationResults = ControlLogic.validateSeek(streamPosition, playbackSession);
            Assert.assertEquals(true, validationResults.isSuccess());
            Assert.assertEquals(IPlayerImplementationControls.ISeekPosition.LIVE_EDGE, validationResults.getRelevantData());
        }
        {
            MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(true) {
                @Override
                public boolean isSeekAllowed() {
                    return false;
                }
            };
            IEnigmaPlayerControls.StreamPosition streamPosition = IEnigmaPlayerControls.StreamPosition.LIVE_EDGE;
            ControlLogic.IValidationResults<IPlayerImplementationControls.ISeekPosition> validationResults = ControlLogic.validateSeek(streamPosition, playbackSession);
            Assert.assertEquals(false, validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.CONTRACT_RESTRICTION_LIMITATION);
        }
        {
            MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(true) {
                @Override
                public boolean isSeekAllowed() {
                    return true;
                }

                @Override
                public boolean isSeekToLiveAllowed() {
                    return false;
                }
            };
            IEnigmaPlayerControls.StreamPosition streamPosition = IEnigmaPlayerControls.StreamPosition.LIVE_EDGE;
            ControlLogic.IValidationResults<IPlayerImplementationControls.ISeekPosition> validationResults = ControlLogic.validateSeek(streamPosition, playbackSession);
            Assert.assertEquals(false, validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.INAPPLICABLE_FOR_CURRENT_STREAM);
        }
    }

    @Test
    public void testValidateSeekWithDirection() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        {
            MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(false);
            boolean seekForward = false;
            boolean seekBackward = false;
            ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateSeek(seekForward, seekBackward, playbackSession);
            Assert.assertEquals(true, validationResults.isSuccess());
        }
        {
            MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(false) {
                @Override
                public boolean isSeekAllowed() {
                    return false;
                }
            };
            boolean seekForward = false;
            boolean seekBackward = false;
            ControlLogic.IValidationResults<Void> validationResults = ControlLogic.validateSeek(seekForward, seekBackward, playbackSession);
            Assert.assertEquals(false, validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.CONTRACT_RESTRICTION_LIMITATION);
        }
        class SubTest {
            final boolean fastForwardEnabled;
            final boolean rewindEnabled;
            final boolean seekForward;

            public SubTest(boolean fastForwardEnabled, boolean rewindEnabled, boolean seekForward) {
                this.fastForwardEnabled = fastForwardEnabled;
                this.rewindEnabled = rewindEnabled;
                this.seekForward = seekForward;
            }

            public ControlLogic.IValidationResults<Void> runValidation() {
                MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(false);
                MockContractRestrictionsValueSource valueSource = new MockContractRestrictionsValueSource();
                valueSource.put(ContractRestriction.FASTFORWARD_ENABLED, fastForwardEnabled);
                valueSource.put(ContractRestriction.REWIND_ENABLED, rewindEnabled);
                playbackSession.setContractRestrictions(new MockContractRestrictions().setValueSource(valueSource));
                return ControlLogic.validateSeek(seekForward, !seekForward, playbackSession);
            }
        }
        {
            SubTest subTest = new SubTest(true, true, true);
            ControlLogic.IValidationResults<Void> validationResults = subTest.runValidation();
            Assert.assertEquals(true, validationResults.isSuccess());
        }
        {
            SubTest subTest = new SubTest(true, false, true);
            ControlLogic.IValidationResults<Void> validationResults = subTest.runValidation();
            Assert.assertEquals(true, validationResults.isSuccess());
        }
        {
            SubTest subTest = new SubTest(false, true, true);
            ControlLogic.IValidationResults<Void> validationResults = subTest.runValidation();
            Assert.assertEquals(false, validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.CONTRACT_RESTRICTION_LIMITATION);
        }
        {
            SubTest subTest = new SubTest(false, false, true);
            ControlLogic.IValidationResults<Void> validationResults = subTest.runValidation();
            Assert.assertEquals(false, validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.CONTRACT_RESTRICTION_LIMITATION);
        }
        {
            SubTest subTest = new SubTest(true, true, false);
            ControlLogic.IValidationResults<Void> validationResults = subTest.runValidation();
            Assert.assertEquals(true, validationResults.isSuccess());
        }
        {
            SubTest subTest = new SubTest(false, true, false);
            ControlLogic.IValidationResults<Void> validationResults = subTest.runValidation();
            Assert.assertEquals(true, validationResults.isSuccess());
        }
        {
            SubTest subTest = new SubTest(true, false, false);
            ControlLogic.IValidationResults<Void> validationResults = subTest.runValidation();
            Assert.assertEquals(false, validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.CONTRACT_RESTRICTION_LIMITATION);
        }
        {
            SubTest subTest = new SubTest(false, false, false);
            ControlLogic.IValidationResults<Void> validationResults = subTest.runValidation();
            Assert.assertEquals(false, validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.CONTRACT_RESTRICTION_LIMITATION);
        }
    }

    @Test
    public void testValidateProgramJump() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        {
            ControlLogic.IValidationResults<Long> validationResults = ControlLogic.validateProgramJump(false, null);
            Assert.assertEquals(false, validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.INAPPLICABLE_FOR_CURRENT_STREAM);
        }
        {
            MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(false);
            playbackSession.setStreamPrograms(null);
            ControlLogic.IValidationResults<Long> validationResults = ControlLogic.validateProgramJump(false, playbackSession);

            Assert.assertEquals(false, validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.INAPPLICABLE_FOR_CURRENT_STREAM);
        }
        {
            MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(false);
            final Counter getNeighbouringSectionStartOffsetCalled = new Counter();
            MockStreamPrograms streamPrograms = new MockStreamPrograms() {
                @Override
                public Long getNeighbouringSectionStartOffset(long fromOffset, boolean searchBackwards) {
                    Assert.assertEquals(searchBackwards, false);
                    getNeighbouringSectionStartOffsetCalled.count();
                    return 17823L;
                }
            };
            playbackSession.setStreamPrograms(streamPrograms);
            ControlLogic.IValidationResults<Long> validationResults = ControlLogic.validateProgramJump(false, playbackSession);

            getNeighbouringSectionStartOffsetCalled.assertOnce();

            Assert.assertEquals(true, validationResults.isSuccess());

            Assert.assertEquals(Long.valueOf(17823L),validationResults.getRelevantData());
        }
        {
            ControlLogic.IValidationResults<Long> validationResults = ControlLogic.validateProgramJump(true, null);
            Assert.assertEquals(false, validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.INAPPLICABLE_FOR_CURRENT_STREAM);
        }
        {
            MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(false);
            playbackSession.setStreamPrograms(null);
            ControlLogic.IValidationResults<Long> validationResults = ControlLogic.validateProgramJump(true, playbackSession);

            Assert.assertEquals(false, validationResults.isSuccess());

            assertRejectedWithType(validationResults, IControlResultHandler.RejectReasonType.INAPPLICABLE_FOR_CURRENT_STREAM);
        }
        {
            MockInternalPlaybackSession playbackSession = new MockInternalPlaybackSession(false);
            final Counter getNeighbouringSectionStartOffsetCalled = new Counter();
            MockStreamPrograms streamPrograms = new MockStreamPrograms() {
                @Override
                public Long getNeighbouringSectionStartOffset(long fromOffset, boolean searchBackwards) {
                    Assert.assertEquals(searchBackwards, true);
                    getNeighbouringSectionStartOffsetCalled.count();
                    return 17823L;
                }
            };
            playbackSession.setStreamPrograms(streamPrograms);
            ControlLogic.IValidationResults<Long> validationResults = ControlLogic.validateProgramJump(true, playbackSession);

            getNeighbouringSectionStartOffsetCalled.assertOnce();

            Assert.assertEquals(true, validationResults.isSuccess());

            Assert.assertEquals(Long.valueOf(17823L),validationResults.getRelevantData());
        }
    }

    private static void assertRejectedWithType(ControlLogic.IValidationResults<?> validationResults, final IControlResultHandler.RejectReasonType expectedRejectReason) {
        final Counter callbackCalled = new Counter();
        ((ControlLogic.IFailedValidationResults) validationResults).triggerCallback(new BasePlayerImplementationControlResultHandler() {
            @Override
            public void onRejected(IControlResultHandler.IRejectReason rejectReason) {
                Assert.assertEquals(expectedRejectReason, rejectReason.getType());
                callbackCalled.count();
            }
        });
        callbackCalled.assertOnce();
    }
}
