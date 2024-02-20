package com.redbeemedia.enigma.core.context;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import androidx.annotation.StringDef;
import java.lang.annotation.Retention;

public enum AppType {
    APP("app");

    private final String value;

    AppType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static AppType getDefault() {
        return APP;
    }
}
