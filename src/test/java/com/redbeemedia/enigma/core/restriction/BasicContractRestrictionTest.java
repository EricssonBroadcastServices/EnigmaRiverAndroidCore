package com.redbeemedia.enigma.core.restriction;

import org.junit.Assert;
import org.junit.Test;

public class BasicContractRestrictionTest {
    @Test
    public void testBasicContractRestriction() {
        final BasicContractRestriction<Boolean> basicContractRestriction = new BasicContractRestriction(Boolean.class, "simpleFlag");

        MockContractRestrictionsValueSource valueSource = new MockContractRestrictionsValueSource();
        valueSource.put("simpleFlag", true);
        Assert.assertEquals(true, basicContractRestriction.getValue(valueSource));

        valueSource = new MockContractRestrictionsValueSource();
        valueSource.put("simpleFlag", false);
        Assert.assertEquals(false, basicContractRestriction.getValue(valueSource));

        valueSource = new MockContractRestrictionsValueSource();
        Assert.assertEquals(null, basicContractRestriction.getValue(valueSource));
    }
}
