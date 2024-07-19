// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.analytics;

/*package-protected*/ interface IEventProperty<E extends IAnalyticsEventType, T> {
    String getName();
    boolean skipIfNull();
    boolean isMandatory();
}
