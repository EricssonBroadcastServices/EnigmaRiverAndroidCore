// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util;

public class OpenContainer<T> {
    public volatile T value;

    public OpenContainer(T value) {
        this.value = value;
    }
}
