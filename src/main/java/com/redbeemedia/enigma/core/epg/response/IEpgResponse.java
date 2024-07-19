// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.epg.response;

import com.redbeemedia.enigma.core.epg.IProgram;

import java.util.List;

public interface IEpgResponse {
    long getStartUtcMillis();
    long getEndUtcMillis();
    IProgram getProgramAt(long utcMillis);
    List<IProgram> getPrograms();
}
