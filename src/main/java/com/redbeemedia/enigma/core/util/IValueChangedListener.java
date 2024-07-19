// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util;

public interface IValueChangedListener<T> extends IInternalListener {
    void onValueChanged(T oldValue, T newValue);
}
