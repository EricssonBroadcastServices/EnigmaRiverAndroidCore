package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.epg.IProgram;

/*package-protected*/ interface IStreamPrograms {
    IProgram getProgram();

    IProgram getProgramForEntitlementCheck();

    Long getNeighbouringSectionStartOffset(long fromOffset, boolean searchBackwards);
}
