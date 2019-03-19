package com.redbeemedia.enigma.core.exposure.query;

import com.redbeemedia.enigma.core.util.IStringAppendable;
import com.redbeemedia.enigma.core.util.StringAppendable;
import com.redbeemedia.enigma.core.util.UrlPath;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;

public class QueryParameterSetTest {

    @Test
    public void testApplyEmpty() throws MalformedURLException {
        QueryParameterSet queryParameterSet = new QueryParameterSet();
        UrlPath urlPath = new UrlPath("http://www.example.com/unittest");
        urlPath = queryParameterSet.applyAll(urlPath);
        Assert.assertEquals("http://www.example.com/unittest?",urlPath.toURL().toString());
    }

    @Test
    public void testApplyMultiple() throws MalformedURLException {
        QueryParameterSet queryParameterSet = new QueryParameterSet();
        queryParameterSet.add(new MockQueryParam("param1=true"));
        queryParameterSet.add(new MockQueryParam("user=Tester"));

        UrlPath urlPath = new UrlPath("http://www.example.com/unittest");
        urlPath = queryParameterSet.applyAll(urlPath);
        Assert.assertEquals("http://www.example.com/unittest?param1=true&user=Tester",urlPath.toURL().toString());
    }

    @Test
    public void testApplyToUrlWithQuery() throws MalformedURLException {
        QueryParameterSet queryParameterSet = new QueryParameterSet();
        queryParameterSet.add(new MockQueryParam("param1=true"));
        queryParameterSet.add(new MockQueryParam("user=Tester"));

        UrlPath urlPath = new UrlPath("http://www.example.com/unittest?otherParam=x");
        urlPath = queryParameterSet.applyAll(urlPath);
        Assert.assertEquals("http://www.example.com/unittest?otherParam=x&param1=true&user=Tester",urlPath.toURL().toString());
    }

    private static class MockQueryParam implements IQueryParameter<String> {
        private String value;

        public MockQueryParam(String value) {
            this.value = value;
        }

        @Override
        public void setValue(String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public IStringAppendable apply(IStringAppendable urlPath) {
            return urlPath.append(value);
        }
    }
}
