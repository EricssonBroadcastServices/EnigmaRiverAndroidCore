// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.epg.impl;

import com.redbeemedia.enigma.core.epg.IProgram;
import com.redbeemedia.enigma.core.epg.MockProgram;
import com.redbeemedia.enigma.core.player.listener.BaseEnigmaPlayerListener;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EpgResponseTest {
    @Test
    public void testEmptyList() {
        List<IProgram> programs = new ArrayList<>();
        EpgResponse epgResponse = new EpgResponse(programs, 1000, 2000);
        Assert.assertEquals(1000, epgResponse.getStartUtcMillis());
        Assert.assertEquals(2000, epgResponse.getEndUtcMillis());
        Assert.assertEquals(0, epgResponse.getPrograms().size());
        Assert.assertEquals(null, epgResponse.getProgramAt(0));
        Assert.assertEquals(null, epgResponse.getProgramAt(1500));
        Assert.assertEquals(null, epgResponse.getProgramAt(2500));
    }

    @Test
    public void testBackToBackPrograms() {
        List<IProgram> programs = new ArrayList<>();
        programs.add(new MockProgram("pr1",500, 1000));
        programs.add(new MockProgram("pr2",1000, 2000));
        programs.add(new MockProgram("pr3",2000, 3000));
        programs.add(new MockProgram("pr4",4000, 5000));
        EpgResponse epgResponse = new EpgResponse(programs, 1000, 6000);
        Assert.assertEquals(1000, epgResponse.getStartUtcMillis());
        Assert.assertEquals(6000, epgResponse.getEndUtcMillis());
        Assert.assertEquals(4, epgResponse.getPrograms().size());
        Assert.assertEquals(null, epgResponse.getProgramAt(500));
        Assert.assertEquals(null, epgResponse.getProgramAt(750));
        Assert.assertEquals("pr2", epgResponse.getProgramAt(1000).toString());
        Assert.assertEquals("pr2", epgResponse.getProgramAt(1999).toString());
        Assert.assertEquals("pr3", epgResponse.getProgramAt(2000).toString());
        Assert.assertEquals("pr3", epgResponse.getProgramAt(2999).toString());
        Assert.assertEquals("pr3", epgResponse.getProgramAt(3000).toString());
        Assert.assertEquals(null, epgResponse.getProgramAt(3001));
        Assert.assertEquals(null, epgResponse.getProgramAt(3500));
        Assert.assertEquals("pr4", epgResponse.getProgramAt(4000).toString());
        Assert.assertEquals("pr4", epgResponse.getProgramAt(5000).toString());
        Assert.assertEquals(null, epgResponse.getProgramAt(5001));
        Assert.assertEquals(null, epgResponse.getProgramAt(6000));
        Assert.assertEquals(null, epgResponse.getProgramAt(7000));
    }

    @Test
    public void testOverlappingPrograms1() {
        List<IProgram> programs = new ArrayList<>();
        programs.add(new MockProgram("big",500, 3000));
        programs.add(new MockProgram("small",1000, 2000));
        EpgResponse epgResponse = new EpgResponse(programs, 0, 5000);
        Assert.assertEquals(0, epgResponse.getStartUtcMillis());
        Assert.assertEquals(5000, epgResponse.getEndUtcMillis());
        Assert.assertEquals(2, epgResponse.getPrograms().size());

        Assert.assertEquals("big", epgResponse.getProgramAt(500).toString());
        Assert.assertEquals("big", epgResponse.getProgramAt(999).toString());
        Assert.assertEquals("small", epgResponse.getProgramAt(1000).toString());
        Assert.assertEquals("small", epgResponse.getProgramAt(1500).toString());
        Assert.assertEquals("small", epgResponse.getProgramAt(2000).toString());
        Assert.assertEquals("big", epgResponse.getProgramAt(2001).toString());
        Assert.assertEquals("big", epgResponse.getProgramAt(2999).toString());
        Assert.assertEquals(null, epgResponse.getProgramAt(4000));
        Assert.assertEquals(null, epgResponse.getProgramAt(5000));
        Assert.assertEquals(null, epgResponse.getProgramAt(6000));
    }
}
