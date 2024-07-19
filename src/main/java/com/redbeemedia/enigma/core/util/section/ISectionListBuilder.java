// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util.section;

public interface ISectionListBuilder<T> {
    ISectionList<T> build();
    void putItem(long start, long end, T item);

    /**
     * Cut away everything before {@code start} and after {@code end}.
     * @param start
     * @param end
     */
    void trim(long start, long end);
}
