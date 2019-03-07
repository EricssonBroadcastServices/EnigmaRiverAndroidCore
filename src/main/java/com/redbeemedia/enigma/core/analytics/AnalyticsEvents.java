package com.redbeemedia.enigma.core.analytics;

/*package-protected*/ class AnalyticsEvents {

    public static final AnalyticsErrorEvent ERROR = new AnalyticsErrorEvent();

    public static class AnalyticsErrorEvent implements IAnalyticsEventType {
        @Override
        public String getName() {
            return "Playback.Error";
        }

        public final EventProperty<AnalyticsErrorEvent, Integer> CODE = new EventProperty<>("Code");
        public final EventProperty<AnalyticsErrorEvent, String> MESSAGE = new EventProperty<>("Message");
        public final EventProperty<AnalyticsErrorEvent, String> DETAILS = new EventProperty<>("Details");
    }
}
