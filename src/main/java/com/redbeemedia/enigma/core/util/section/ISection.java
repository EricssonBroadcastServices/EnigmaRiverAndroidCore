// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util.section;

public interface ISection<T> {
    T getItem();
    ISection<T> getPrevious();
    ISection<T> getNext();
    long getStart(); //section[n].end == section[n+1].start
    long getEnd();
}
