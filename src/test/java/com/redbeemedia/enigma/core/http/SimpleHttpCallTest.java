package com.redbeemedia.enigma.core.http;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SimpleHttpCallTest {
    @Test
    public void testGET() {
        IHttpCall httpCall = SimpleHttpCall.GET();
        Assert.assertEquals("GET", httpCall.getRequestMethod());
    }

    @Test
    public void testPOST() throws IOException {
        IHttpCall httpCall = SimpleHttpCall.POST(new byte[]{1,2,3});

        Assert.assertEquals("POST", httpCall.getRequestMethod());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        httpCall.writeBodyTo(byteArrayOutputStream);
        byte[] writtenData = byteArrayOutputStream.toByteArray();
        Assert.assertArrayEquals(new byte[]{1,2,3}, writtenData);

        httpCall = SimpleHttpCall.POST(new byte[0]);

        Assert.assertEquals("POST", httpCall.getRequestMethod());
        byteArrayOutputStream = new ByteArrayOutputStream();
        httpCall.writeBodyTo(byteArrayOutputStream);
        writtenData = byteArrayOutputStream.toByteArray();
        Assert.assertArrayEquals(new byte[]{}, writtenData);


        httpCall = SimpleHttpCall.POST(new Object() {
            @Override
            public String toString() {
                return "The Data";
            }
        });

        Assert.assertEquals("POST", httpCall.getRequestMethod());
        byteArrayOutputStream = new ByteArrayOutputStream();
        httpCall.writeBodyTo(byteArrayOutputStream);
        writtenData = byteArrayOutputStream.toByteArray();
        Assert.assertArrayEquals("The Data".getBytes(StandardCharsets.UTF_8), writtenData);

        httpCall = SimpleHttpCall.POST(null);
        Assert.assertEquals("POST", httpCall.getRequestMethod());
        byteArrayOutputStream = new ByteArrayOutputStream();
        httpCall.writeBodyTo(byteArrayOutputStream);
        writtenData = byteArrayOutputStream.toByteArray();
        Assert.assertArrayEquals(new byte[]{}, writtenData);
    }
}
