package com.redbeemedia.enigma.core.util;

import android.util.JsonReader;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

public class JsonReaderUtilTest {
    @Test
    public void testReadStringList() throws IOException {
        List<String> list = JsonReaderUtil.readArray(new JsonReader(new StringReader("[\"one\",\"two\"]")), String.class);
        Assert.assertEquals(Arrays.asList("one","two"), list);
    }

    @Test
    public void testReadIntList() throws IOException {
        List<Integer> list = JsonReaderUtil.readArray(new JsonReader(new StringReader("[42,1337]")), Integer.class);
        Assert.assertEquals(Arrays.asList(42,1337), list);
    }

    @Test
    public void testReadLongList() throws IOException {
        List<Long> list = JsonReaderUtil.readArray(new JsonReader(new StringReader("[42,1337]")), Long.class);
        Assert.assertEquals(Arrays.asList(42L,1337L), list);
    }

    @Test
    public void testReadBooleanList() throws IOException {
        List<Boolean> list = JsonReaderUtil.readArray(new JsonReader(new StringReader("[true,false,true]")), Boolean.class);
        Assert.assertEquals(Arrays.asList(true,false,true), list);
    }

    @Test
    public void testReadDoubleList() throws IOException {
        List<Double> list = JsonReaderUtil.readArray(new JsonReader(new StringReader("[42.1337,1337.42]")), Double.class);
        Assert.assertEquals(Arrays.asList(42.1337,1337.42), list);
    }
}
