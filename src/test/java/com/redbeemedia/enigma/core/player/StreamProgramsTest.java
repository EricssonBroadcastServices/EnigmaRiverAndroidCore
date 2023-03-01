package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;
import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.epg.MockProgram;
import com.redbeemedia.enigma.core.epg.response.MockEpgResponse;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.MockHttpHandler;

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
        IProgram program0 = new MockProgram("program0", 0, 2000);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", 5000, 6000);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", 6000, 7000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", 10000, 15000);
        programs.add(program3);
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(5000, 20000, programs), false, 0L);

        Assert.assertSame(program1, streamPrograms.getProgram());
    }

    @Test
    public void testOffsetUsedForEntitlementCheck() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<IProgram> programs = new ArrayList<>();
        long currentTime = new Date().getTime();
        IProgram program0 = new MockProgram("program0", currentTime, currentTime + (110 * 1000));
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", currentTime + (120 * 1000), currentTime + (240 * 1000));
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", currentTime + (241 * 1000), currentTime + (340 * 1000));
        programs.add(program2);

        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(currentTime, currentTime + (340 * 1000), programs), true, 0L);

        Assert.assertSame(program1, streamPrograms.getProgramForEntitlementCheck());
    }

    @Test
    public void testNeighbouringProgramNoGap() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<IProgram> programs = new ArrayList<>();
        IProgram program0 = new MockProgram("program0", 0, 5000);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", 5000, 6000);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", 6000, 10000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", 10000, 15000);
        programs.add(program3);
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(2000, 15000, programs), false, 0L);

        Assert.assertEquals(null, streamPrograms.getNeighbouringSectionStartOffset(-3000, true));
        Assert.assertSame(program0, streamPrograms.getProgram());

        Assert.assertEquals(null, streamPrograms.getNeighbouringSectionStartOffset(0, true));
        //Here we search from offset 0. The stream starts at utc 2000, so we would start in
        // program0 (0 <= 2000 < 5000). Searing forwards we end up in program1, which starts at
        // utc 5000 which is at offset 3000 since the stream starts at utc 2000.
        Assert.assertEquals(Long.valueOf(5000), streamPrograms.getNeighbouringSectionStartOffset(0, false));

        Assert.assertEquals(null, streamPrograms.getNeighbouringSectionStartOffset(4000, true));
        Assert.assertEquals(Long.valueOf(5000), streamPrograms.getNeighbouringSectionStartOffset(4000, false));

        Assert.assertEquals(Long.valueOf(5000), streamPrograms.getNeighbouringSectionStartOffset(9000, true));
        Assert.assertEquals(Long.valueOf(10000), streamPrograms.getNeighbouringSectionStartOffset(9000, false));

        Assert.assertEquals(Long.valueOf(10000), streamPrograms.getNeighbouringSectionStartOffset(500000, true));
        Assert.assertEquals(null, streamPrograms.getNeighbouringSectionStartOffset(500000, false));
    }

    @Test
    public void testNeighbouringProgramWithTrailingGap() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<IProgram> programs = new ArrayList<>();
        IProgram program0 = new MockProgram("program0", 0, 5000);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", 5000, 6000);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", 6000, 10000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", 10000, 15000);
        programs.add(program3);
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(2000, 20000, programs), false, 0L);

        Assert.assertEquals(Long.valueOf(10000), streamPrograms.getNeighbouringSectionStartOffset(9000, false));
        Assert.assertEquals(program0, streamPrograms.getProgram());
        Assert.assertEquals(null, streamPrograms.getNeighbouringSectionStartOffset(15000, false));
    }

    @Test
    public void testGetProgramWithOffsetForLiveWhenDeviceTimeAhead() throws InterruptedException {
        MockEnigmaRiverContextInitialization initialization = new MockEnigmaRiverContextInitialization();
        MockHttpHandler httpHandler = new MockHttpHandler();
        initialization.setHttpHandler(httpHandler);
        MockEnigmaRiverContext.resetInitialize(initialization);

        long deviceTime = (new Date()).getTime();
        // deviceTime is ahead
        long serverTime = deviceTime - 10000;
        httpHandler.queueResponse(new HttpStatus(200,"OK"),serverTime+"");

        List<IProgram> programs = new ArrayList<>();
        IProgram program0 = new MockProgram("program0", deviceTime - 20000, deviceTime - 10000);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", deviceTime - 10000, deviceTime);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", deviceTime, deviceTime + 15000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", deviceTime + 15000, deviceTime + 50000);
        programs.add(program3);
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(deviceTime - 20000, deviceTime + 50000, programs), true, 10000L);

        // pass 21 seconds
        Thread.sleep(21000);
        Assert.assertEquals(program2, streamPrograms.getProgram());

        // pass another 15 seconds
        Thread.sleep(15000);
        Assert.assertEquals(program3, streamPrograms.getProgram());
    }

    @Test
    public void testGetProgramWithOffsetForLiveWhenDeviceBehind() throws InterruptedException {
        MockEnigmaRiverContextInitialization initialization = new MockEnigmaRiverContextInitialization();
        MockHttpHandler httpHandler = new MockHttpHandler();
        initialization.setHttpHandler(httpHandler);
        MockEnigmaRiverContext.resetInitialize(initialization);

        long deviceTime = (new Date()).getTime();
        // deviceTime is ahead
        long serverTime = deviceTime + 10000;
        httpHandler.queueResponse(new HttpStatus(200,"OK"),serverTime+"");

        List<IProgram> programs = new ArrayList<>();
        IProgram program0 = new MockProgram("program0", deviceTime - 10000, deviceTime);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", deviceTime  , deviceTime + 10000);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", deviceTime + 10000, deviceTime + 20000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", deviceTime + 20000, deviceTime + 30000);
        programs.add(program3);
        IProgram program4 = new MockProgram("program3", deviceTime + 30000, deviceTime + 50000);
        programs.add(program4);
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(deviceTime - 10000, deviceTime + 50000, programs), true, -10000L);

        // pass 16 seconds
        Thread.sleep(19000);
        Assert.assertEquals(program2, streamPrograms.getProgram());

        // pass 16 seconds
        Thread.sleep(16000);
        Assert.assertEquals(program4, streamPrograms.getProgram());
    }


    @Test
    public void testNeighbouringProgramWithGap() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<IProgram> programs = new ArrayList<>();
        IProgram program0 = new MockProgram("program0", 0, 2000);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", 5000, 6000);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", 6000, 7000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", 10000, 15000);
        programs.add(program3);
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(0, 15000, programs), false, 0L);

        long pos = 0;
        Assert.assertSame(program0, streamPrograms.getProgram());

        //Gap
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, false);
        Assert.assertEquals(2000, pos);
        Assert.assertSame(program0, streamPrograms.getProgram());

        //Program 1
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, false);
        Assert.assertEquals(5000, pos);
        Assert.assertSame(program0, streamPrograms.getProgram());

        System.out.println(pos);
        //End
        Assert.assertEquals(new Long(6000), streamPrograms.getNeighbouringSectionStartOffset(pos, false));
    }

    @Test
    public void testNeighbouringProgramWithSplitProgram_IterateForward() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<IProgram> programs = new ArrayList<>();
        IProgram program0 = new MockProgram("program0", 0, 6000);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", 2000, 4000);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", 6000, 7000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", 10000, 15000);
        programs.add(program3);
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(0, 15000, programs), false, 0L);

        Assert.assertSame(program0, streamPrograms.getProgram());

        //Program 1
        long pos = 0;
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, false);
        Assert.assertEquals(2000, pos);
        Assert.assertSame(program0, streamPrograms.getProgram());
    }

    @Test
    public void testNeighbouringProgramWithSplitProgram_IterateBackward() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<IProgram> programs = new ArrayList<>();
        IProgram program0 = new MockProgram("program0", 0, 6000);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", 2000, 4000);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", 6000, 7000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", 10000, 15000);
        programs.add(program3);
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(1000, 15000, programs), false, 0L);

        //End
        long pos = 14000;
        Assert.assertSame(program0, streamPrograms.getProgram());

        //Program 3
        pos = streamPrograms.getNeighbouringSectionStartOffset(pos, true);
        Assert.assertEquals(7000, pos);
        Assert.assertSame(program0, streamPrograms.getProgram());

        Assert.assertEquals(Long.valueOf(6000), streamPrograms.getNeighbouringSectionStartOffset(pos, true));
    }

    @Test
    public void testStreamProgramsWithinBounds() {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization());

        List<IProgram> programs = new ArrayList<>();
        IProgram program_m1 = new MockProgram("program-1", -1000, 0);
        programs.add(program_m1);
        IProgram program0 = new MockProgram("program0", 0, 6000);
        programs.add(program0);
        IProgram program1 = new MockProgram("program1", 2000, 4000);
        programs.add(program1);
        IProgram program2 = new MockProgram("program2", 6000, 7000);
        programs.add(program2);
        IProgram program3 = new MockProgram("program3", 10000, 15000);
        programs.add(program3);
        long streamStart = 3000;
        long streamEnd = 7000;
        StreamPrograms streamPrograms = new StreamPrograms(new MockEpgResponse(streamStart, streamEnd, programs), false, 0L);


        //Search backwards from start of stream --> Expect null
        Assert.assertNull(streamPrograms.getNeighbouringSectionStartOffset(0, true));

        //Search forwards from end of stream --> Expect null
        Assert.assertNull(streamPrograms.getNeighbouringSectionStartOffset((streamEnd - streamStart), false));


        Assert.assertEquals(Long.valueOf(1000L), streamPrograms.getNeighbouringSectionStartOffset(-1, true));
        Assert.assertEquals(Long.valueOf(4000L), streamPrograms.getNeighbouringSectionStartOffset(-1, false));

        Assert.assertEquals(Long.valueOf(2000L), streamPrograms.getNeighbouringSectionStartOffset((streamEnd - streamStart), true));
        Assert.assertEquals(null, streamPrograms.getNeighbouringSectionStartOffset((streamEnd - streamStart), false));
    }
}
