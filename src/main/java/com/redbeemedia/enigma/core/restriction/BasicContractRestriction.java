// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.restriction;

public final class BasicContractRestriction<T> implements IContractRestriction<T> {
    private final Class<T> type;
    private final String propertyName;

    public BasicContractRestriction(Class<T> type, String propertyName) {
        this.type = type;
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public T getValue(IContractRestrictionsValueSource valueSource) {
        if(valueSource.hasValue(propertyName, type)) {
            return valueSource.getValue(propertyName, type);
        } else {
            return null;
        }
    }
}
