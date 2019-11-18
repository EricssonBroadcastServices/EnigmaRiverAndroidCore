package com.redbeemedia.enigma.core.virtualui.impl;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.playbacksession.IPlaybackSession;
import com.redbeemedia.enigma.core.player.EnigmaPlayer;
import com.redbeemedia.enigma.core.player.EnigmaPlayerState;
import com.redbeemedia.enigma.core.player.EnigmaPlayerTest;
import com.redbeemedia.enigma.core.player.IEnigmaPlayer;
import com.redbeemedia.enigma.core.player.MockEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.MockPlayerImplementation;
import com.redbeemedia.enigma.core.player.controls.IControlResultHandler;
import com.redbeemedia.enigma.core.player.controls.IEnigmaPlayerControls;
import com.redbeemedia.enigma.core.player.timeline.ITimelinePosition;
import com.redbeemedia.enigma.core.restriction.ContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictions;
import com.redbeemedia.enigma.core.restriction.MockContractRestrictions;
import com.redbeemedia.enigma.core.session.MockSession;
import com.redbeemedia.enigma.core.testutil.Counter;
import com.redbeemedia.enigma.core.testutil.ReflectionUtil;
import com.redbeemedia.enigma.core.virtualui.BaseVirtualButtonListener;
import com.redbeemedia.enigma.core.virtualui.IVirtualButton;
import com.redbeemedia.enigma.core.virtualui.IVirtualControls;
import com.redbeemedia.enigma.core.virtualui.IVirtualControlsSettings;
import com.redbeemedia.enigma.core.virtualui.VirtualControlsSettings;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class VirtualControlsTest {
    @Test(expected = NullPointerException.class)
    public void testEnigmaPlayerNotNullable() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        VirtualControls.create(null, new VirtualControlsSettings());
    }

    @Test
    public void testVirtualControlsNotNullable() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        final IEnigmaPlayer enigmaPlayer = new EnigmaPlayer(new MockSession(), new MockPlayerImplementation());
        try {
            VirtualControls.create(enigmaPlayer, null);
            Assert.fail("Expected "+NullPointerException.class.getSimpleName());
        } catch (NullPointerException e) {
            //Expected
        }

        IVirtualControls virtualControls = VirtualControls.create(enigmaPlayer, new VirtualControlsSettings());
        Assert.assertNotNull(virtualControls);
    }

    @Test
    public void testButtonsNotNull() throws InvocationTargetException, IllegalAccessException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        IEnigmaPlayer enigmaPlayer = new EnigmaPlayer(new MockSession(), new MockPlayerImplementation());
        IVirtualControls virtualControls = VirtualControls.create(enigmaPlayer, new VirtualControlsSettings());

        Collection<Method> methods = ReflectionUtil.getPublicMethods(IVirtualControls.class, new ReflectionUtil.IPublicMethodFilter() {
            @Override
            public boolean matches(boolean isStatic, Class<?> returnType, String name, Class<?>[] parametersTypes) {
                return !isStatic && IVirtualButton.class.equals(returnType);
            }
        });

        Assert.assertFalse("Expected at least one method to test", methods.isEmpty());

        for(Method method : methods) {
            Assert.assertNotNull(method.getName()+" returned null!", method.invoke(virtualControls));
        }
    }

    @Test
    public void testPlayButton() throws Exception {
        testVirtualButton(PlayButton.class, new IVirtualButtonTest() {
            @Override
            public void makeButtonEnabled(TestVirtualButtonContainer virtualButtonContainer) {
                virtualButtonContainer.playerState = EnigmaPlayerState.LOADED;
            }

            @Override
            public void makeButtonDisabled(TestVirtualButtonContainer virtualButtonContainer) {
                virtualButtonContainer.playerState = EnigmaPlayerState.PLAYING;
            }

            @Override
            public void assertClicked(TestVirtualButtonContainer virtualButtonContainer) {
                Assert.assertEquals("[controls.start()]", virtualButtonContainer.controlLog.toString());
            }
        });
    }

    @Test
    public void testPauseButton() throws Exception {
        testVirtualButton(PauseButton.class, new IVirtualButtonTest() {
            @Override
            public void makeButtonEnabled(TestVirtualButtonContainer virtualButtonContainer) {
                virtualButtonContainer.playerState = EnigmaPlayerState.PLAYING;
                virtualButtonContainer.contractRestrictions = new MockContractRestrictions() {
                    @Override
                    public <T> T getValue(IContractRestriction<T> restriction, T fallback) {
                        if(restriction == ContractRestriction.TIMESHIFT_ENABLED) {
                            return (T) Boolean.TRUE;
                        }
                        return super.getValue(restriction, fallback);
                    }
                };
            }

            @Override
            public void makeButtonDisabled(TestVirtualButtonContainer virtualButtonContainer) {
                virtualButtonContainer.playerState = EnigmaPlayerState.PLAYING;
                virtualButtonContainer.contractRestrictions = new MockContractRestrictions() {
                    @Override
                    public <T> T getValue(IContractRestriction<T> restriction, T fallback) {
                        if(restriction == ContractRestriction.TIMESHIFT_ENABLED) {
                            return (T) Boolean.FALSE;
                        }
                        return super.getValue(restriction, fallback);
                    }
                };
            }

            @Override
            public void assertClicked(TestVirtualButtonContainer virtualButtonContainer) {
                Assert.assertEquals("[controls.pause()]", virtualButtonContainer.controlLog.toString());
            }
        });
    }

    @Test
    public void testFastForwardButton() throws Exception {
        testVirtualButton(FastForwardButton.class, new IVirtualButtonTest() {
            @Override
            public void makeButtonEnabled(TestVirtualButtonContainer virtualButtonContainer) {
                virtualButtonContainer.playerState = EnigmaPlayerState.PLAYING;
                virtualButtonContainer.contractRestrictions = new MockContractRestrictions() {
                    @Override
                    public <T> T getValue(IContractRestriction<T> restriction, T fallback) {
                        if(restriction == ContractRestriction.FASTFORWARD_ENABLED) {
                            return (T) Boolean.TRUE;
                        }
                        return super.getValue(restriction, fallback);
                    }
                };
            }

            @Override
            public void makeButtonDisabled(TestVirtualButtonContainer virtualButtonContainer) {
                virtualButtonContainer.playerState = EnigmaPlayerState.PLAYING;
                virtualButtonContainer.contractRestrictions = new MockContractRestrictions() {
                    @Override
                    public <T> T getValue(IContractRestriction<T> restriction, T fallback) {
                        if(restriction == ContractRestriction.FASTFORWARD_ENABLED) {
                            return (T) Boolean.FALSE;
                        }
                        return super.getValue(restriction, fallback);
                    }
                };
            }

            @Override
            public void assertClicked(TestVirtualButtonContainer virtualButtonContainer) {
                Assert.assertEquals("[controls.seekTo(ITimelinePosition)]", virtualButtonContainer.controlLog.toString());
            }
        });
    }

    @Test
    public void testRewindButton() throws Exception {
        testVirtualButton(RewindButton.class, new IVirtualButtonTest() {
            @Override
            public void makeButtonEnabled(TestVirtualButtonContainer virtualButtonContainer) {
                virtualButtonContainer.playerState = EnigmaPlayerState.PLAYING;
                virtualButtonContainer.contractRestrictions = new MockContractRestrictions() {
                    @Override
                    public <T> T getValue(IContractRestriction<T> restriction, T fallback) {
                        if(restriction == ContractRestriction.REWIND_ENABLED) {
                            return (T) Boolean.TRUE;
                        }
                        return super.getValue(restriction, fallback);
                    }
                };
            }

            @Override
            public void makeButtonDisabled(TestVirtualButtonContainer virtualButtonContainer) {
                virtualButtonContainer.playerState = EnigmaPlayerState.PLAYING;
                virtualButtonContainer.contractRestrictions = new MockContractRestrictions() {
                    @Override
                    public <T> T getValue(IContractRestriction<T> restriction, T fallback) {
                        if(restriction == ContractRestriction.REWIND_ENABLED) {
                            return (T) Boolean.FALSE;
                        }
                        return super.getValue(restriction, fallback);
                    }
                };
            }

            @Override
            public void assertClicked(TestVirtualButtonContainer virtualButtonContainer) {
                Assert.assertEquals("[controls.seekTo(ITimelinePosition)]", virtualButtonContainer.controlLog.toString());
            }
        });
    }

    private <T extends AbstractVirtualButtonImpl> void testVirtualButton(Class<T> buttonType, IVirtualButtonTest virtualButtonTest) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        TestVirtualButtonContainer virtualButtonContainer = new TestVirtualButtonContainer();

        IVirtualButton button = buttonType.getConstructor(IVirtualButtonContainer.class).newInstance(virtualButtonContainer);
        Assert.assertTrue(virtualButtonContainer.buttons.contains(button));
        virtualButtonContainer.refreshButtons();

        virtualButtonTest.makeButtonEnabled(virtualButtonContainer);
        virtualButtonContainer.refreshButtons();
        Assert.assertTrue("Expected "+buttonType.getSimpleName()+" to be enabled", button.isEnabled());
        button.click();
        virtualButtonTest.assertClicked(virtualButtonContainer);

        final Counter onStateChangedCalled = new Counter();
        button.addListener(new BaseVirtualButtonListener() {
            @Override
            public void onStateChanged() {
                onStateChangedCalled.count();
            }
        });

        onStateChangedCalled.assertNone();
        virtualButtonTest.makeButtonDisabled(virtualButtonContainer);
        virtualButtonContainer.refreshButtons();
        onStateChangedCalled.assertOnce();
        Assert.assertFalse("Expected "+buttonType.getSimpleName()+" to be disabled", button.isEnabled());
    }

    private interface IVirtualButtonTest {
        void makeButtonEnabled(TestVirtualButtonContainer virtualButtonContainer);
        void makeButtonDisabled(TestVirtualButtonContainer virtualButtonContainer);
        void assertClicked(TestVirtualButtonContainer virtualButtonContainer);
    }

    private static class TestVirtualButtonContainer implements IVirtualButtonContainer {
        public EnigmaPlayerState playerState = EnigmaPlayerState.IDLE;
        public final List<AbstractVirtualButtonImpl> buttons = new ArrayList<>();
        private final IEnigmaPlayerControls playerControls = new MockEnigmaPlayerControls() {
            @Override
            public void start(IControlResultHandler resultHandler) {
                super.start(resultHandler);
                logControl("start()");
            }

            @Override
            public void pause(IControlResultHandler resultHandler) {
                super.pause(resultHandler);
                logControl("pause()");
            }

            @Override
            public void seekTo(long millis, IControlResultHandler resultHandler) {
                super.seekTo(millis, resultHandler);
                logControl("seekTo(long)");
            }

            @Override
            public void seekTo(StreamPosition streamPosition, IControlResultHandler resultHandler) {
                super.seekTo(streamPosition, resultHandler);
                logControl("seekTo(StreamPosition)");
            }

            @Override
            public void seekTo(ITimelinePosition timelinePos, IControlResultHandler resultHandler) {
                resultHandler.onDone();
                logControl("seekTo(ITimelinePosition)");
            }

            @Override
            public void stop(IControlResultHandler resultHandler) {
                super.stop(resultHandler);
                logControl("stop()");
            }
        };
        public IContractRestrictions contractRestrictions = null;
        public IVirtualControlsSettings settings = new VirtualControlsSettings();
        private StringBuilder controlLog = new StringBuilder();
        private IPlaybackSession playbackSession = null;

        private IEnigmaPlayer enigmaPlayer = new EnigmaPlayerTest.EnigmaPlayerWithMockedTimeProvider(new MockSession(), new MockPlayerImplementation()) {

            @Override
            public EnigmaPlayerState getState() {
                return playerState;
            }

            @Override
            public IEnigmaPlayerControls getControls() {
                return playerControls;
            }
        };

        private void logControl(String call) {
            controlLog.append("[controls."+call+"]");
        }

        @Override
        public void addButton(AbstractVirtualButtonImpl virtualButton) {
            buttons.add(virtualButton);
        }

        @Override
        public IEnigmaPlayerControls getPlayerControls() {
            return playerControls;
        }

        @Override
        public EnigmaPlayerState getPlayerState() {
            return playerState;
        }

        @Override
        public IContractRestrictions getContractRestrictions() {
            return contractRestrictions;
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
            return playbackSession;
        }

        public void refreshButtons() {
            for(AbstractVirtualButtonImpl button : buttons) {
                button.refresh();
            }
        }
    }
}
