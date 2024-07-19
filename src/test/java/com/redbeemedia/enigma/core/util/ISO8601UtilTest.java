// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util;

import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

public class ISO8601UtilTest {
    @Test
    public void testParsing() throws ParseException {
        ISO8601Util.IISO8601Parser parser = ISO8601Util.newParser();

        Assert.assertEquals(1569325020000L,  parser.parse("2019-09-24T11:37:00+00:00"));
        Assert.assertEquals(1569325020000L,  parser.parse("2019-09-24T09:37:00-02:00"));
        Assert.assertEquals(1571909820000L,  parser.parse("2019-10-24T09:37:00Z"));
        Assert.assertEquals(1571909820123L,  parser.parse("2019-10-24T09:37:00.123Z"));

        Assert.assertEquals(1569325020000L,  parser.parse("2019-09-24t11:37:00+00:00"));
        Assert.assertEquals(1569325020000L,  parser.parse("2019-09-24t09:37:00-02:00"));
        Assert.assertEquals(1571909820000L,  parser.parse("2019-10-24t09:37:00Z"));
        Assert.assertEquals(1571909820123L,  parser.parse("2019-10-24t09:37:00.123z"));
    }

    @Test(expected = ParseException.class)
    public void testInvalidFormat() throws ParseException {
        ISO8601Util.IISO8601Parser parser = ISO8601Util.newParser();
        parser.parse("2019-09-24T11:37:00?00:00");
    }

    @Test
    public void testWriting() {
        ISO8601Util.IISO8601Writer utcWriter = ISO8601Util.newWriter(TimeZone.getTimeZone("UTC"));

        Assert.assertEquals("2019-09-24T11:37:00Z",  utcWriter.toIso8601(1569325020000L));
        Assert.assertEquals("2019-09-24T11:37:00Z", utcWriter.toIso8601(1569325020000L));
        Assert.assertEquals("2019-10-24T09:37:00Z", utcWriter.toIso8601(1571909820000L));
        Assert.assertEquals("2019-10-24T09:37:00.123Z", utcWriter.toIso8601(1571909820123L));

        utcWriter = ISO8601Util.newWriter(TimeZone.getTimeZone("GMT+2"));
        Assert.assertEquals("2019-09-24T15:16:01+02:00", utcWriter.toIso8601(1569330961000L));
        Assert.assertEquals("1970-01-01T02:04:02.534+02:00", utcWriter.toIso8601(242534L));

        utcWriter = ISO8601Util.newWriter(TimeZone.getTimeZone("GMT-3"));
        Assert.assertEquals("2019-09-24T10:16:01-03:00", utcWriter.toIso8601(1569330961000L));
        Assert.assertEquals("1970-01-03T16:22:14-03:00", utcWriter.toIso8601(242534000L));

        TimeZone mockZone = new SimpleTimeZone(1000*60*30, "MOCK");
        utcWriter = ISO8601Util.newWriter(mockZone);
        Assert.assertEquals("2019-09-24T13:46:00+00:30", utcWriter.toIso8601(1569330960000L));
    }

    @Test
    public void testSelfConsistency() throws ParseException {
        ISO8601Util.IISO8601Parser parser = ISO8601Util.newParser();

        ISO8601Util.IISO8601Writer utcWriter = ISO8601Util.newWriter(TimeZone.getTimeZone("UTC"));
        ISO8601Util.IISO8601Writer gmt_p6_writer = ISO8601Util.newWriter(TimeZone.getTimeZone("GMT+6"));
        ISO8601Util.IISO8601Writer gmt_m2_writer = ISO8601Util.newWriter(TimeZone.getTimeZone("GMT-2"));

        assertConsistent(parser, utcWriter, 1569330960000L);
        assertConsistent(parser, gmt_m2_writer, 1569330960000L);
        assertConsistent(parser, gmt_m2_writer, 1569330960000L);

        assertConsistent(parser, utcWriter, 1569330961234L);
        assertConsistent(parser, gmt_m2_writer, 1569330961234L);
        assertConsistent(parser, gmt_m2_writer, 1569330961234L);
    }

    private void assertConsistent(ISO8601Util.IISO8601Parser parser, ISO8601Util.IISO8601Writer writer, long utcTestMillis) throws ParseException {
        String isoString = writer.toIso8601(utcTestMillis);
        Assert.assertEquals(utcTestMillis, parser.parse(isoString));
        Assert.assertEquals(isoString, writer.toIso8601(parser.parse(isoString)));
    }
}
