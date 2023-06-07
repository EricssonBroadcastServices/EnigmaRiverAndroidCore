package com.redbeemedia.enigma.core.analytics;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.testutil.ReflectionUtil;
import com.redbeemedia.enigma.core.time.MockTimeProvider;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class AnalyticsReportedEventTest<E extends IAnalyticsEventType> {
    private final E eventType;
    private final IEventSender eventSender;
    private List<IAnalyticsEventType> reportedEvents;
    private List<IEventProperty<?,?>> reportedProperties;
    private AnalyticsReporter analyticsReporter;

    public AnalyticsReportedEventTest(String testName, E eventType, IEventSender eventSender) {
        this.eventType = eventType;
        this.eventSender = eventSender;
    }

    @Before
    public void initialize() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());
        this.reportedEvents = new ArrayList<>();
        this.reportedProperties = new ArrayList<>();
        this.analyticsReporter = new AnalyticsReporter(new MockTimeProvider(), jsonObject -> {},0l) {
            @Override
            protected <E extends IAnalyticsEventType> IAnalyticsEventBuilder<E> newEventBuilder(E eventType) throws JSONException {
                reportedEvents.add(eventType);
                return new IAnalyticsEventBuilder<E>() {
                    @Override
                    public <T> void addData(IEventProperty<? super E, T> property, T value) throws JSONException {
                        if(value != null) {
                            reportedProperties.add(property);
                        }
                    }

                    @Override
                    public JSONObject build() {
                        return null;
                    }
                };
            }
        };
    }

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object> analyticsEventTypes() throws IllegalAccessException {
        Collection<IAnalyticsEventType> eventTypes = new ArrayList<>();
        Collection<Field> fields = ReflectionUtil.getFields(AnalyticsEvents.class, (modifiers, fieldType, name, declaringClass) -> {
            boolean publicStaticFinal = Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
            return publicStaticFinal && IAnalyticsEventType.class.isAssignableFrom(fieldType);
        });
        for(Field field : fields) {
            eventTypes.add((IAnalyticsEventType) field.get(null));
        }
        Collection<Object> testParameters = new ArrayList<>();

        for(IAnalyticsEventType eventType : eventTypes) {
            testParameters.add(new Object[]{eventType.getName()+"Test", eventType, getSendMethod(eventType)});
        }

        Assert.assertFalse("No parameters found",testParameters.isEmpty());

        return testParameters;
    }

    private static IEventSender getSendMethod(IAnalyticsEventType eventType) {
        if(eventType == AnalyticsEvents.ERROR) {
            return reporter -> reporter.playbackError(new UnexpectedError("MockError"));
        } else if(eventType == AnalyticsEvents.DEVICE_INFO) {
            return reporter -> reporter.deviceInfo("MockCDN");
        } else if(eventType == AnalyticsEvents.CREATED) {
            return reporter -> reporter.playbackCreated("mockAsset");
        } else if(eventType == AnalyticsEvents.HANDSHAKE_STARTED) {
            return reporter -> reporter.playbackHandshakeStarted("mockAsset");
        } else if (eventType == AnalyticsEvents.PLAYER_READY) {
            return reporter -> reporter.playbackPlayerReady(72442L, "MockTech", "1.0");
        } else if (eventType == AnalyticsEvents.STARTED) {
            return reporter -> reporter.playbackStarted(35737L, "test", "http://mock.example.com/media/mock",
                    null, null, null,575778,
                    "null", "null" ,
                    "","null","null");
        } else if (eventType == AnalyticsEvents.PAUSED) {
            return reporter -> reporter.playbackPaused(37357L);
        } else if (eventType == AnalyticsEvents.RESUMED) {
            return reporter -> reporter.playbackResumed(4662L);
        } else if (eventType == AnalyticsEvents.COMPLETED) {
            return reporter -> reporter.playbackCompleted(373577L);
        } else if (eventType == AnalyticsEvents.ABORTED) {
            return reporter -> reporter.playbackAborted(82772L);
        } else if (eventType == AnalyticsEvents.HEARTBEAT) {
            return reporter -> reporter.playbackHeartbeat(8272L);
        } else if (eventType == AnalyticsEvents.APP_BACKGROUNDED) {
            return reporter -> reporter.playbackAppBackgrounded(2662L);
        } else if (eventType == AnalyticsEvents.APP_RESUMED) {
            return reporter -> reporter.playbackAppResumed(34654L);
        } else if (eventType == AnalyticsEvents.GRACE_PERIOD_ENDED) {
            return reporter -> reporter.playbackGracePeriodEnded(882277L);
        } else if (eventType == AnalyticsEvents.BITRATE_CHANGED) {
            return reporter -> reporter.playbackBitrateChanged(12345, 1000);
        } else if (eventType == AnalyticsEvents.BUFFERING_STARTED) {
            return reporter -> reporter.playbackBufferingStarted(35373L);
        } else if (eventType == AnalyticsEvents.BUFFERING_STOPPED) {
            return reporter -> reporter.playbackBufferingStopped(32727L);
        } else if (eventType == AnalyticsEvents.SCRUBBED_TO) {
            return reporter -> reporter.playbackScrubbedTo(246246L);
        } else if (eventType == AnalyticsEvents.DRM) {
            return reporter -> reporter.playbackDrm(246246L);
        } else if (eventType == AnalyticsEvents.STOP_CASTING) {
            return reporter -> reporter.playbackStopCasting(246246L);
        } else if (eventType == AnalyticsEvents.START_CASTING) {
            return reporter -> reporter.playbackStartCasting(246200L);
        } else if (eventType == AnalyticsEvents.AD_FAILED) {
            return reporter -> reporter.playbackAdFailed(246246L);
        } else if (eventType == AnalyticsEvents.PROGRAM_CHANGED) {
            return reporter -> reporter.playbackProgramChanged(8346L, "MockProgram");
        } else if (eventType == AnalyticsEvents.AD_STARTED) {
            return reporter -> reporter.playbackAdStarted(12345L, "started_test_ad");
        } else if (eventType == AnalyticsEvents.AD_COMPLETED) {
            return reporter -> reporter.playbackAdCompleted(65536L, "completed_test_ad");
        } else {
            return null;
        }
    }

    @Test
    public void testMandatoryProperties() {
        String missingEventSenderMessage = "No send-method returned for "+eventType.getName()+". See "+ AnalyticsReportedEventTest.class.getSimpleName()+"::getSendMethod";
        Assert.assertNotNull(missingEventSenderMessage, eventSender);

        Collection<IEventProperty<? super E, ?>> properties = getProperties(eventType);
        Assert.assertFalse("No properties found for "+eventType.getName(), properties.isEmpty());

        Assert.assertFalse("Event type built before event initiated", reportedEvents.contains(eventType));

        eventSender.sendEventUsing(analyticsReporter);

        if(!reportedEvents.contains(eventType)) {
            if(reportedEvents.isEmpty()) {
                Assert.fail("No events reported after send-method execution!");
            } else {
                StringBuilder reportedEventNames = new StringBuilder();
                for(IAnalyticsEventType reportedEventType : reportedEvents) {
                    if(reportedEventNames.length() > 0) {
                        reportedEventNames.append(", ");
                    }
                    reportedEventNames.append(reportedEventType.getName());
                }
                Assert.fail("Event type "+eventType.getName()+" not built! Wrong send-method returned in "+ AnalyticsReportedEventTest.class.getSimpleName()+"::getSendMethod? (got "+reportedEventNames+")");
            }
        }

        for(IEventProperty<? super E, ?> property : properties) {
            if(property.isMandatory()) {
                Assert.assertTrue( "Mandatory property "+property.getName()+" of "+eventType.getName()+" not sent", reportedProperties.contains(property));
            }
        }

    }

    private static <E extends IAnalyticsEventType> Collection<IEventProperty<? super E, ?>> getProperties(E eventType) {
        Collection<IEventProperty<? super E,?>> properties = new ArrayList<>();
        Class<? extends IAnalyticsEventType> eventTypeClass = eventType.getClass();
        Collection<Field> fields = ReflectionUtil.getFields(eventTypeClass, (modifiers, fieldType, name, declaringClass) -> !Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && IEventProperty.class.isAssignableFrom(fieldType));
        for(Field field : fields) {
            try {
                properties.add((IEventProperty<? super E, ?>) field.get(eventType));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return properties;
    }

    public interface IEventSender {
        void sendEventUsing(AnalyticsReporter reporter);
    }
}
