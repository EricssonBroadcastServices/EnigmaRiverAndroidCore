// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util;

public class StringAppendable implements IStringAppendable {
    private StringBuilder stringBuilder;

    public StringAppendable(StringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
    }

    public StringAppendable() {
        this(new StringBuilder());
    }

    @Override
    public StringAppendable append(String string) {
        stringBuilder.append(string);
        return this;
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
