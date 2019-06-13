package com.redbeemedia.enigma.core.analytics;

/*package-protected*/ class EventProperty<E extends IAnalyticsEventType, T> implements IEventProperty<E,T> {
    private final String name;
    private final boolean skipIfNull;

    public EventProperty(String name) {
        this(name, false);
    }

    public EventProperty(String name, boolean skipIfNull) {
        this.name = name;
        this.skipIfNull = skipIfNull;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean skipIfNull() {
        return skipIfNull;
    }
}
