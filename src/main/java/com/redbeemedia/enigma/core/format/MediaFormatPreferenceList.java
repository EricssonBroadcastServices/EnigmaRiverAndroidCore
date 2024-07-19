// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MediaFormatPreferenceList {
    private List<EnigmaMediaFormat> preferenceList = new ArrayList<>();

    public MediaFormatPreferenceList putFirst(EnigmaMediaFormat enigmaMediaFormat) {
        return put(0, enigmaMediaFormat);
    }

    public MediaFormatPreferenceList putLast(EnigmaMediaFormat enigmaMediaFormat) {
        return put(-1, enigmaMediaFormat);
    }

    /**
     *  Inserts a {@link EnigmaMediaFormat} at {@code index} and pushes back the current format at
     *  that position (if any).
     *
     *  The parameter {@code index} should be within the range {@code (index >= 0 && index < getList().size())}
     *  or {@code -1}, where {@code -1} indicates that the format should be inserted last.
     *
     * @param index The index in the list where the media format will be after this method call. Or
     *             -1 if insertion should be at the end of the list (insert last).
     * @param enigmaMediaFormat Non-null media format
     * @return this {@link MediaFormatPreferenceList} for chaining
     */
    public MediaFormatPreferenceList put(int index, EnigmaMediaFormat enigmaMediaFormat) {
        if(enigmaMediaFormat == null) {
            throw new IllegalArgumentException();
        }
        boolean insertLast = index == -1;
        int currentIndex = preferenceList.indexOf(enigmaMediaFormat);
        if(currentIndex != -1) {
            if(currentIndex == index) {
                return this;
            } else if(currentIndex < index) {
                index--;
            }
            preferenceList.remove(currentIndex);
        }
        if(insertLast) {
            preferenceList.add(enigmaMediaFormat);
        } else {
            preferenceList.add(index, enigmaMediaFormat);
        }
        return this;
    }

    /**
     * Returns an unmodifiable copy of the preference list
     */
    public List<EnigmaMediaFormat> getList() {
        return Collections.unmodifiableList(new ArrayList<>(preferenceList));
    }

    @Override
    public int hashCode() {
        int result = 1;

        for(EnigmaMediaFormat mediaFormat : preferenceList) {
            result = 31 * result + mediaFormat.hashCode();
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof MediaFormatPreferenceList)) {
            return false;
        }
        MediaFormatPreferenceList other = (MediaFormatPreferenceList) obj;
        if(this.preferenceList.size() != other.preferenceList.size()) {
            return false;
        }

        for(int i = 0; i < this.preferenceList.size(); ++i) {
            if(!this.preferenceList.get(i).equals(other.preferenceList.get(i))) {
                return false;
            }
        }

        return true;
    }
}
