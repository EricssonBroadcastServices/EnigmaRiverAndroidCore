package com.redbeemedia.enigma.core.player;

import com.redbeemedia.enigma.core.restriction.IContractRestriction;
import com.redbeemedia.enigma.core.restriction.IContractRestrictionsValueSource;
import com.redbeemedia.enigma.core.restriction.MockContractRestriction;
import com.redbeemedia.enigma.core.restriction.MockContractRestrictionsValueSource;
import com.redbeemedia.enigma.core.testutil.Flag;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class EnigmaContractRestrictionsTest {
    @Test
    public void testNullDefaultsToEmpty() {
        EnigmaContractRestrictions restrictions = new EnigmaContractRestrictions(null);

        final Flag getValueCalled = new Flag();
        restrictions.getValue(new IContractRestriction<String>() {
            @Override
            public String getValue(IContractRestrictionsValueSource valueSource) {
                Assert.assertNotNull(valueSource);
                getValueCalled.setFlag();
                return null;
            }
        }, null);
        getValueCalled.assertSet("getValue(...) never called");

        String stringValue = restrictions.getValue(new MockContractRestriction<String>(null), "defaultTestValue");
        Assert.assertEquals("defaultTestValue", stringValue);

        int intValue = restrictions.getValue(new MockContractRestriction<Integer>(3), -1);
        Assert.assertEquals(3, intValue);
    }

    @Test
    public void testFallback() {
        EnigmaContractRestrictions restrictions = new EnigmaContractRestrictions(new MockContractRestrictionsValueSource());

        String test1 = restrictions.getValue(new MockContractRestriction<String>("definedValue"), "fallback");
        Assert.assertEquals("definedValue", test1);

        String test2 = restrictions.getValue(new MockContractRestriction<>(null), "fallback");
        Assert.assertEquals("fallback", test2);


        int test3 = restrictions.getValue(new MockContractRestriction<>(123), null);
        Assert.assertEquals(123, test3);

        int test4 = restrictions.getValue(new MockContractRestriction<>(null), 5);
        Assert.assertEquals(5, test4);

        int test5 = restrictions.getValue(new MockContractRestriction<>(123), 5);
        Assert.assertEquals(123, test5);

        Integer test6 = restrictions.getValue(new MockContractRestriction<>(null), null);
        Assert.assertNull(test6);
    }

    private static class IntPair {
        public final int a;
        public final int b;

        public IntPair(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof IntPair) && ((IntPair) obj).a == this.a && ((IntPair) obj).b == this.b;
        }
    }

    private static final IContractRestriction<Integer> NEW_VAR_WITH_DEFAULT = new IContractRestriction<Integer>() {
        @Override
        public Integer getValue(IContractRestrictionsValueSource valueSource) {
            if(valueSource.hasValue("newVar", Integer.class)) {
                return valueSource.getValue("newVar", Integer.class);
            } else {
                return -1;
            }
        }
    };

    private static final IContractRestriction<Integer> NEW_VAR_WITHOUT_DEFAULT = new IContractRestriction<Integer>() {
        @Override
        public Integer getValue(IContractRestrictionsValueSource valueSource) {
            if(valueSource.hasValue("newVar", Integer.class)) {
                return valueSource.getValue("newVar", Integer.class);
            } else {
                return null;
            }
        }
    };
    private static final IContractRestriction<IntPair> COMPLEX_NEW_RESTRICTION = new IContractRestriction<IntPair>() {
        @Override
        public IntPair getValue(IContractRestrictionsValueSource valueSource) {
            if(valueSource.hasValue("complexVar", String.class)) {
                String value = valueSource.getValue("complexVar", String.class);
                String parts[] = value.split(",");
                int a = Integer.parseInt(parts[0].substring(1));
                int b = Integer.parseInt(parts[1].substring(0, parts[1].length()-1));
                return new IntPair(a,b);
            } else {
                return null;
            }
        }
    };

    @Test
    public void testWithTypeNotInSdkYet() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("newVar", 1997);
        jsonObject.put("complexVar", "(-1,3)");

        testWithTypeNotInSdkYet(1997, new IntPair(-1,3), new EnigmaContractRestrictions(new JsonObjectValueSource(jsonObject)));

        testWithTypeNotInSdkYet(null, null, new EnigmaContractRestrictions(new JsonObjectValueSource(new JSONObject())));
    }

    private static void testWithTypeNotInSdkYet(Integer newVarValue, IntPair complexVarValue, EnigmaContractRestrictions contractRestrictions) {
        int newVarFallback = 8000;
        IntPair complexVarFallback = new IntPair(4,20);

        boolean hasNewVar = newVarValue != null;
        Assert.assertEquals(hasNewVar ? newVarValue.intValue() : -1, contractRestrictions.getValue(NEW_VAR_WITH_DEFAULT, newVarFallback).intValue());
        Assert.assertEquals(hasNewVar ? newVarValue.intValue() : newVarFallback, contractRestrictions.getValue(NEW_VAR_WITHOUT_DEFAULT, newVarFallback).intValue());

        boolean hasComplexVar = complexVarValue != null;
        Assert.assertEquals(hasComplexVar ? complexVarValue : complexVarFallback, contractRestrictions.getValue(COMPLEX_NEW_RESTRICTION, complexVarFallback));
    }

    @Test
    public void testJsonObjectValueSourceWithNull() {
        JsonObjectValueSource jsonObjectValueSource = new JsonObjectValueSource(null);
        Assert.assertFalse(jsonObjectValueSource.hasValue("any", String.class));
        Assert.assertFalse(jsonObjectValueSource.hasValue("other", Integer.class));
        Assert.assertNull(jsonObjectValueSource.getValue("any", String.class));
        Assert.assertNull(jsonObjectValueSource.getValue("other", Integer.class));
    }

    @Test
    public void testJsonObjectValueSource() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("someInteger", 555);
        jsonObject.put("someIntegerReadAsLong", 123L);
        jsonObject.put("someBigInteger", String.valueOf(Long.MAX_VALUE));
        jsonObject.put("someString", "well-defined");
        JsonObjectValueSource valueSource = new JsonObjectValueSource(jsonObject);

        Assert.assertEquals(true,valueSource.hasValue("someInteger", Integer.class));
        Assert.assertEquals(false,valueSource.hasValue("unknown", Integer.class));

        Assert.assertEquals(555, valueSource.getValue("someInteger", Integer.class).intValue());


        Assert.assertEquals(true,valueSource.hasValue("someIntegerReadAsLong", Integer.class));
        Assert.assertEquals(true,valueSource.hasValue("someIntegerReadAsLong", String.class));
        Assert.assertEquals(true,valueSource.hasValue("someIntegerReadAsLong", Long.class));

        Assert.assertEquals(123,valueSource.getValue("someIntegerReadAsLong", Integer.class).intValue());
        Assert.assertEquals(123L,valueSource.getValue("someIntegerReadAsLong", Long.class).longValue());
        Assert.assertEquals("123",valueSource.getValue("someIntegerReadAsLong", String.class));

        Assert.assertEquals(false,valueSource.hasValue("someBigInteger", Integer.class));
        Assert.assertEquals(true,valueSource.hasValue("someBigInteger", String.class));
        Assert.assertEquals(true,valueSource.hasValue("someBigInteger", Long.class));

        Assert.assertEquals(Long.MAX_VALUE,valueSource.getValue("someBigInteger", Long.class).longValue());
        Assert.assertEquals(String.valueOf(Long.MAX_VALUE),valueSource.getValue("someBigInteger", String.class));

        Assert.assertEquals(false,valueSource.hasValue("undefined", String.class));

        Assert.assertEquals("well-defined",valueSource.getValue("someString", String.class));
        Assert.assertEquals(true,valueSource.hasValue("someString", String.class));
        Assert.assertEquals(false,valueSource.hasValue("someString", Integer.class));
        Assert.assertEquals(false,valueSource.hasValue("someString", Long.class));
    }
}
