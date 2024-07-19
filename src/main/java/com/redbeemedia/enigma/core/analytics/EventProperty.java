// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.analytics;

/*package-protected*/ class EventProperty<E extends IAnalyticsEventType, T> implements IEventProperty<E,T> {
    private final String name;
    private final boolean skipIfNull;
    private final boolean mandatory;

    private EventProperty(String name, boolean skipIfNull, boolean mandatory) {
        this.name = name;
        this.skipIfNull = skipIfNull;
        this.mandatory = mandatory;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean skipIfNull() {
        return skipIfNull;
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    public static <E extends IAnalyticsEventType,T> IEventProperty<E,T> newProperty(String name) {
        return new EventProperty<>(name, false, false);
    }

    public static <E extends IAnalyticsEventType,T> IEventProperty<E,T> newProperty(String name, boolean skipIfNull) {
        return new EventProperty<>(name, skipIfNull, false);
    }

    public static <E extends IAnalyticsEventType,T> IEventProperty<E,T> newMandatoryProperty(String name) {
        return new EventProperty<>(name, false, true);
    }
}
