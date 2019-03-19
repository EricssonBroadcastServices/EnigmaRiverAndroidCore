package com.redbeemedia.enigma.core.analytics;

/*package-protected*/ class EventProperty<E extends IAnalyticsEventType, T> implements IEventProperty<E,T> {
    private final String name;

    public EventProperty(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
