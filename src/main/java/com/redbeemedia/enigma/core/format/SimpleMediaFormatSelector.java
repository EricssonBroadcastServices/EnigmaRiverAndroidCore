// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.format;

import java.util.Collection;

public final class SimpleMediaFormatSelector implements IMediaFormatSelector {
    private final MediaFormatPreferenceList preferenceList;

    public SimpleMediaFormatSelector(EnigmaMediaFormat ...preferenceOrder) {
        this(new MediaFormatPreferenceList());
        for(int i = 0; i < preferenceOrder.length; ++i) {
            this.preferenceList.put(i, preferenceOrder[i]);
        }
    }

    public SimpleMediaFormatSelector(MediaFormatPreferenceList preferenceList) {
        this.preferenceList = preferenceList;
    }

    public MediaFormatPreferenceList getPreferenceList() {
        return preferenceList;
    }

    @Override
    public EnigmaMediaFormat select(EnigmaMediaFormat prospect, Collection<EnigmaMediaFormat> available) {
        for(EnigmaMediaFormat format : preferenceList.getList()) {
            if(available.contains(format)) {
                return format;
            }
        }
        return prospect;
    }
}
