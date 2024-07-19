// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public class ValueChangedCollector<T> extends Collector<IValueChangedListener<T>> implements IValueChangedListener<T> {
    public ValueChangedCollector() {
        super((Class<IValueChangedListener<T>>)(Class<?>) IValueChangedListener.class);
    }

    @Override
    public void onValueChanged(T oldValue, T newValue) {
        forEach(listener -> listener.onValueChanged(oldValue, newValue));
    }
}
