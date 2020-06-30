package com.redbeemedia.enigma.core.format;

import java.util.HashSet;
import java.util.Set;

public final class MediaFormatPreferenceSpec implements IMediaFormatPreferenceSpec {
    private final EnigmaMediaFormat[] preferenceOrder;

    public MediaFormatPreferenceSpec(EnigmaMediaFormat ... preferenceOrder) {
        verifyValidArgument(preferenceOrder);
        this.preferenceOrder = preferenceOrder;
    }

    private static void verifyValidArgument(EnigmaMediaFormat[] preferenceOrder) {
        Set<EnigmaMediaFormat> unique = new HashSet<>();
        for(EnigmaMediaFormat preference : preferenceOrder) {
            if(preference == null) {
                throw new IllegalArgumentException("Preferences must not be null");
            }
            if(!unique.add(preference)) {
                throw new IllegalArgumentException("Duplicate preference "+preference);
            }
        }
    }

    @Override
    public MediaFormatPreferenceList applyPreference(MediaFormatPreferenceList preferenceList) {
        for(int i = 0; i < preferenceOrder.length; ++i) {
            preferenceList.put(i, preferenceOrder[i]);
        }
        return preferenceList;
    }
}
