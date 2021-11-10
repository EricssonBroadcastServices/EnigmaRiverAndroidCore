package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.epg.MockProgram;
import com.redbeemedia.enigma.core.epg.response.MockEpgResponse;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
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
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(5000, 20000, programs),false);

        Assert.assertSame(program1, streamPrograms.getProgramAtOffset(0));
        Assert.assertSame(program1, streamPrograms.getProgramAtOffset(500));
        Assert.assertSame(program2, streamPrograms.getProgramAtOffset(1500));
        Assert.assertSame(program2, streamPrograms.getProgramAtOffset(1000));
        Assert.assertNull(streamPrograms.getProgramAtOffset(2500));
        Assert.assertSame(program3, streamPrograms.getProgramAtOffset(5500));
        Assert.assertNull(streamPrograms.getProgramAtOffset(15500));
    }

    @Test
    public void testNeighbouringProgramNoGap() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<IProgram> programs = new ArrayList<>();
        IProgram program0 = new MockProgram("program0",0,5000);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", 5000, 6000);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", 6000, 10000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", 10000, 15000);
        programs.add(program3);
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(2000, 15000, programs),false);

        Assert.assertEquals(null, streamPrograms.getNeighbouringSectionStartOffset(-3000, true));
        Assert.assertSame(program0, streamPrograms.getProgramAtOffset(streamPrograms.getNeighbouringSectionStartOffset(-3000, false)));

        Assert.assertEquals(null,streamPrograms.getNeighbouringSectionStartOffset(0, true));
        //Here we search from offset 0. The stream starts at utc 2000, so we would start in
        // program0 (0 <= 2000 < 5000). Searing forwards we end up in program1, which starts at
        // utc 5000 which is at offset 3000 since the stream starts at utc 2000.
        Assert.assertEquals(Long.valueOf(3000), streamPrograms.getNeighbouringSectionStartOffset(0, false));

        Assert.assertEquals(Long.valueOf(3000), streamPrograms.getNeighbouringSectionStartOffset(4000, true));
        Assert.assertEquals(Long.valueOf(8000), streamPrograms.getNeighbouringSectionStartOffset(4000, false));

        Assert.assertEquals(Long.valueOf(4000), streamPrograms.getNeighbouringSectionStartOffset(9000, true));
        Assert.assertEquals(null, streamPrograms.getNeighbouringSectionStartOffset(9000, false));

        Assert.assertEquals(Long.valueOf(8000), streamPrograms.getNeighbouringSectionStartOffset(500000, true));
        Assert.assertEquals(null, streamPrograms.getNeighbouringSectionStartOffset(500000, false));
    }

    @Test
    public void testNeighbouringProgramWithTrailingGap() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<IProgram> programs = new ArrayList<>();
        IProgram program0 = new MockProgram("program0",0,5000);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", 5000, 6000);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", 6000, 10000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", 10000, 15000);
        programs.add(program3);
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(2000, 20000, programs),false);

        Assert.assertEquals(Long.valueOf(13000), streamPrograms.getNeighbouringSectionStartOffset(9000, false));
        Assert.assertEquals(null, streamPrograms.getProgramAtOffset(13000));
        Assert.assertEquals(null, streamPrograms.getNeighbouringSectionStartOffset(13000, false));
    }

    @Test
    public void testGetProgramWithOffsetForLive() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        long currentTime = (new Date()).getTime();

        List<IProgram> programs = new ArrayList<>();
        IProgram program0 = new MockProgram("program0", currentTime - 20, currentTime - 10);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", currentTime - 10, currentTime);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", currentTime, currentTime + 10);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", currentTime + 10, currentTime + 20);
        programs.add(program3);
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(currentTime - 20, currentTime + 20, programs), true);
        Assert.assertEquals(program3, streamPrograms.getProgramAtOffset(10));
    }

    @Test
    public void testNeighbouringProgramWithGap() {
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
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(0, 15000, programs),false);

        long pos = 0;
        Assert.assertSame(program0, streamPrograms.getProgramAtOffset(pos));

        //Gap
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, false);
        Assert.assertEquals(2000, pos);
        Assert.assertSame(null, streamPrograms.getProgramAtOffset(pos));

        //Program 1
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, false);
        Assert.assertEquals(5000, pos);
        Assert.assertSame(program1, streamPrograms.getProgramAtOffset(pos));

        //Program 2
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, false);
        Assert.assertEquals(6000, pos);
        Assert.assertSame(program2, streamPrograms.getProgramAtOffset(pos));

        //Second gap
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, false);
        Assert.assertEquals(7000, pos);
        Assert.assertSame(null, streamPrograms.getProgramAtOffset(pos));

        //Program 3
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, false);
        Assert.assertEquals(10000, pos);
        Assert.assertSame(program3, streamPrograms.getProgramAtOffset(pos));

        //End
        Assert.assertEquals(null, streamPrograms.getNeighbouringSectionStartOffset(pos, false));
    }

    @Test
    public void testNeighbouringProgramWithSplitProgram_IterateForward() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<IProgram> programs = new ArrayList<>();
        IProgram program0 = new MockProgram("program0",0,6000);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", 2000, 4000);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", 6000, 7000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", 10000, 15000);
        programs.add(program3);
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(0, 15000, programs),false);

        Assert.assertSame(program0, streamPrograms.getProgramAtOffset(1000));
        Assert.assertSame(program1, streamPrograms.getProgramAtOffset(2000));
        Assert.assertSame(program1, streamPrograms.getProgramAtOffset(3000));
        Assert.assertSame(program0, streamPrograms.getProgramAtOffset(4000));
        Assert.assertSame(program0, streamPrograms.getProgramAtOffset(5000));
        Assert.assertSame(program2, streamPrograms.getProgramAtOffset(6000));

        //Program 0
        long pos = 0;
        Assert.assertSame(program0, streamPrograms.getProgramAtOffset(pos));

        //Program 1
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, false);
        Assert.assertEquals(2000, pos);
        Assert.assertSame(program1, streamPrograms.getProgramAtOffset(pos));

        //Program 0 again
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, false);
        Assert.assertEquals(4000, pos);
        Assert.assertSame(program0, streamPrograms.getProgramAtOffset(pos));

        //Program 2
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, false);
        Assert.assertEquals(6000, pos);
        Assert.assertSame(program2, streamPrograms.getProgramAtOffset(pos));

        //Gap
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, false);
        Assert.assertEquals(7000, pos);
        Assert.assertSame(null, streamPrograms.getProgramAtOffset(pos));

        //Program 3
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, false);
        Assert.assertEquals(10000, pos);
        Assert.assertSame(program3, streamPrograms.getProgramAtOffset(pos));
    }

    @Test
    public void testNeighbouringProgramWithSplitProgram_IterateBackward() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<IProgram> programs = new ArrayList<>();
        IProgram program0 = new MockProgram("program0",0,6000);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", 2000, 4000);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", 6000, 7000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", 10000, 15000);
        programs.add(program3);
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(1000, 15000, programs),false);

        //End
        long pos = 14000;
        Assert.assertSame(null, streamPrograms.getProgramAtOffset(pos));

        //Program 3
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, true);
        Assert.assertEquals(9000, pos);
        Assert.assertSame(program3, streamPrograms.getProgramAtOffset(pos));

        //Gap
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, true);
        Assert.assertEquals(6000, pos);
        Assert.assertSame(null, streamPrograms.getProgramAtOffset(pos));

        //Program 2
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, true);
        Assert.assertEquals(5000, pos);
        Assert.assertSame(program2, streamPrograms.getProgramAtOffset(pos));

        //Program 0
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, true);
        Assert.assertEquals(3000, pos);
        Assert.assertSame(program0, streamPrograms.getProgramAtOffset(pos));

        //Program 1
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, true);
        Assert.assertEquals(1000, pos);
        Assert.assertSame(program1, streamPrograms.getProgramAtOffset(pos));

        //Program 0
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, true);
        Assert.assertEquals(0, pos);
        Assert.assertSame(program0, streamPrograms.getProgramAtOffset(pos));
    }

    @Test
    public void testStreamProgramsWithinBounds() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<IProgram> programs = new ArrayList<>();
        IProgram program_m1 = new MockProgram("program-1",-1000,0);
        programs.add(program_m1);
        IProgram program0 = new MockProgram("program0",0,6000);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", 2000, 4000);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", 6000, 7000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", 10000, 15000);
        programs.add(program3);
        long streamStart = 3000;
        long streamEnd = 7000;
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(streamStart, streamEnd, programs),false);


        //Search backwards from start of stream --> Expect null
        Assert.assertNull(streamPrograms.getNeighbouringSectionStartOffset(0, true));

        //Search forwards from end of stream --> Expect null
        Assert.assertNull(streamPrograms.getNeighbouringSectionStartOffset((streamEnd-streamStart)-1, false));


        Assert.assertEquals(null, streamPrograms.getNeighbouringSectionStartOffset(-1, true));
        Assert.assertEquals(Long.valueOf(3000L-streamStart), streamPrograms.getNeighbouringSectionStartOffset(-1, false));

        Assert.assertEquals(Long.valueOf(6000L-streamStart), streamPrograms.getNeighbouringSectionStartOffset((streamEnd-streamStart), true));
        Assert.assertEquals(null, streamPrograms.getNeighbouringSectionStartOffset((streamEnd-streamStart), false));
    }
}
