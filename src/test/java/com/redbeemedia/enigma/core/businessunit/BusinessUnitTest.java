// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.businessunit;

import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class BusinessUnitTest {

    @Test
    public void testConstructor() throws MalformedURLException {
        BusinessUnit businessUnit = new BusinessUnit("custom", "business");
        Assert.assertEquals("custom", businessUnit.getCustomerName());
        Assert.assertEquals("business", businessUnit.getName());
    }

    @Test
    public void testBaseUrl() throws MalformedURLException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setExposureBaseUrl("http://www.fakeurl.fake"));
        BusinessUnit businessUnit = new BusinessUnit("esbes6", "e57nbxe");
        Assert.assertEquals(new URL("http://www.fakeurl.fake/v1/customer/esbes6/businessunit/e57nbxe"), businessUnit.getApiBaseUrl().toURL());
    }
}
