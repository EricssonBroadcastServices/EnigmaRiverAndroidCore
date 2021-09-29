package com.redbeemedia.enigma.core.marker;

public class Localized {
    private final String locale;
    private final String title;

    public Localized(String locale, String title) {
        this.locale = locale;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getLocale() {
        return locale;
    }
}
