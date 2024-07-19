// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.epg.IProgram;

/*package-protected*/ interface IStreamPrograms {
    IProgram getProgram();

    IProgram getProgramForEntitlementCheck();

    Long getNeighbouringSectionStartOffset(long fromOffset, boolean searchBackwards);
}
