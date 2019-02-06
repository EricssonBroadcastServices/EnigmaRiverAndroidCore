package com.redbeemedia.enigma.core.exposure.query;

import com.redbeemedia.enigma.core.util.UrlPath;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;

public class QueryParameterBuilderTest {
    @Test
    public void testSetter() throws MalformedURLException {
        IQueryParameterSet mockSet = new MockQueryParameterSet();
        IQueryParameter<Integer> number = QueryParameterBuilder.create(mockSet,Integer.class, "number");

        UrlPath urlPath = new UrlPath("http://www.example.com/test?");
        number.setValue(13);
        Assert.assertEquals("http://www.example.com/test?number=13", applyTo(urlPath, number).toURL().toString());
        number.setValue(42);
        Assert.assertEquals("http://www.example.com/test?number=42", applyTo(urlPath, number).toURL().toString());
    }

    @Test
    public void testCreateWithDefaultValue() throws MalformedURLException {
        IQueryParameterSet mockSet = new MockQueryParameterSet();
        IQueryParameter<Integer> number = QueryParameterBuilder.create(mockSet,Integer.class, "number", 123);

        UrlPath urlPath = new UrlPath("http://www.example.com/test?");
        Assert.assertEquals("http://www.example.com/test?number=123", applyTo(urlPath, number).toURL().toString());
        number.setValue(42);
        Assert.assertEquals("http://www.example.com/test?number=42", applyTo(urlPath, number).toURL().toString());
    }

    @Test
    public void testCreateWithoutDefaultValue() throws MalformedURLException {
        IQueryParameterSet mockSet = new MockQueryParameterSet();
        IQueryParameter<Integer> number = QueryParameterBuilder.create(mockSet,Integer.class, "number");

        UrlPath urlPath = new UrlPath("http://www.example.com/test?");
        Assert.assertEquals("http://www.example.com/test?", applyTo(urlPath, number).toURL().toString());
        number.setValue(42);
        Assert.assertEquals("http://www.example.com/test?number=42", applyTo(urlPath, number).toURL().toString());
        number.setValue(1337);
        Assert.assertEquals("http://www.example.com/test?number=1337", applyTo(urlPath, number).toURL().toString());
    }

    private static UrlPath applyTo(UrlPath urlPath, IQueryParameter<?> queryParameter) {
        return (UrlPath) queryParameter.apply(urlPath);
    }

    private static class MockQueryParameterSet implements IQueryParameterSet {

        @Override
        public void add(IQueryParameter<?> queryParameter) {
        }

        @Override
        public UrlPath applyAll(UrlPath urlPath) {
            throw new UnsupportedOperationException();
        }
    }
}
