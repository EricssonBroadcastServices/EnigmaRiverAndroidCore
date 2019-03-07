package com.redbeemedia.enigma.core.analytics;

import com.redbeemedia.enigma.core.BuildConfig;
import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.error.PlayerImplementationError;
import com.redbeemedia.enigma.core.time.ITimeProvider;

import org.json.JSONException;

public class AnalyticsReporter {
    private final IAnalyticsHandler analyticsHandler;
    private final ITimeProvider timeProvider;

    public AnalyticsReporter(ITimeProvider timeProvider, IAnalyticsHandler analyticsHandler) {
        this.timeProvider = timeProvider;
        this.analyticsHandler = analyticsHandler;
    }

    public void error(Error error) {
        try {
            IAnalyticsEventBuilder<AnalyticsEvents.AnalyticsErrorEvent> builder = newEventBuilder(AnalyticsEvents.ERROR);
            builder.addData(AnalyticsEvents.ERROR.CODE, error.getErrorCode());

            if(error instanceof PlayerImplementationError) {
                PlayerImplementationError playerImplementationError = (PlayerImplementationError) error;
                builder.addData(new PlayerSpecificErrorCode(playerImplementationError), playerImplementationError.getInternalErrorCode());
            }

            builder.addData(AnalyticsEvents.ERROR.MESSAGE, error.getClass().getSimpleName());
            builder.addData(AnalyticsEvents.ERROR.DETAILS, error.getTrace());

            analyticsHandler.onAnalytics(builder.build());
        } catch (Exception e) {
            if(BuildConfig.DEBUG) {
                throw new RuntimeException(e);
            } else {
                e.printStackTrace();
            }
        }
    }

    private <E extends IAnalyticsEventType> IAnalyticsEventBuilder<E> newEventBuilder(E eventType) throws JSONException {
        return new JsonAnalyticsEventBuilder(eventType.getName(), timeProvider.getTime());
    }

    private static class PlayerSpecificErrorCode implements IEventProperty<AnalyticsEvents.AnalyticsErrorEvent, Integer> {
        private final String name;

        public PlayerSpecificErrorCode(PlayerImplementationError playerImplementationError) {
            this.name = playerImplementationError.getInternalErrorCodeFieldName();
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
