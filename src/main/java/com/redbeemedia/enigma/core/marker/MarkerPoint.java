package com.redbeemedia.enigma.core.marker;

import java.util.List;

/**
 * "markerPoints" : [ {
 * "type" : "INTRO",
 * "offset" : 0,
 * "endOffset" : 8000,
 * "localized" : [ {
 * "locale" : "sv",
 * "title" : "Dweeb får mat"
 * }, {
 * "locale" : "en",
 * "title" : "Skip Recap"
 * } ]
 * }
 */
public class MarkerPoint {

    private final MarkerType type;
    // in milliseconds
    private long offset;
    private long endOffset;
    private final List<Localized> localized;

    public MarkerPoint(MarkerType type, int endOffset, int offset, List<Localized> localizedList) {
        this.type = type;
        this.endOffset = endOffset;
        this.offset = offset;
        this.localized = localizedList;
    }

    public MarkerType getType() {
        return type;
    }

    public long getOffset() {
        return offset;
    }

    public long getEndOffset() {
        return endOffset;
    }

    public List<Localized> getLocalized() {
        return localized;
    }

    public boolean isIntro() {
        return type == MarkerType.INTRO;
    }

    public String getTitle(String locale) {
        for (Localized localized : localized) {
            if (localized.getLocale().equalsIgnoreCase(locale)) {
                return localized.getTitle();
            }
        }
        return null;
    }
}





