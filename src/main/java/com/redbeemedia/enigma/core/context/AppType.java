// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.context;

public enum AppType {
    APP("android_app"),

    ANDROID_TV("androidtv_app");

    private final String value;

    AppType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static AppType getDefault(Boolean isTV) {
        if(isTV){
            return ANDROID_TV;
        } else {
            return APP;
        }
    }
}
