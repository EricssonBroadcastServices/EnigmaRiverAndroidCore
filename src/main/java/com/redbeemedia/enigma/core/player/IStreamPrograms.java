package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.epg.IProgram;

/*package-protected*/ interface IStreamPrograms {
    IProgram getProgramAtOffset(long offset);
    IProgram getNext(IProgram program);
    IProgram getPrevious(IProgram program);
}
