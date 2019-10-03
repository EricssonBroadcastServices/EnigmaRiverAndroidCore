package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.epg.MockProgram;
import com.redbeemedia.enigma.core.epg.response.MockEpgResponse;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class StreamProgramsTest {
    @Test
    public void testOffsetUsed() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<IProgram> programs = new ArrayList<>();
        IProgram program0 = new MockProgram("program0",0,2000);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", 5000, 6000);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", 6000, 7000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", 10000, 15000);
        programs.add(program3);
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(5000, 20000, programs));

        Assert.assertSame(program1, streamPrograms.getProgramAtOffset(0));
        Assert.assertSame(program1, streamPrograms.getProgramAtOffset(500));
        Assert.assertSame(program2, streamPrograms.getProgramAtOffset(1500));
        Assert.assertSame(program2, streamPrograms.getProgramAtOffset(1000));
        Assert.assertNull(streamPrograms.getProgramAtOffset(2500));
        Assert.assertSame(program3, streamPrograms.getProgramAtOffset(5500));
        Assert.assertNull(streamPrograms.getProgramAtOffset(15500));

    }
}
