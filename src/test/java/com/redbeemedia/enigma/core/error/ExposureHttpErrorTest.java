package com.redbeemedia.enigma.core.error;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class ExposureHttpErrorTest {

    @Test
    public void testGetHttpError() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", "UNKNOWN_BUSINESS_UNIT. If the business unit cannot be found.");
        jsonObject.put("httpCode", 404);
        ExposureHttpError exposureHttpError = new ExposureHttpError(jsonObject);
        Assert.assertEquals(404, exposureHttpError.getHttpCode());
        Assert.assertEquals("UNKNOWN_BUSINESS_UNIT. If the business unit cannot be found.", exposureHttpError.getMessage());
    }

    @Test
    public void testIsError() {
        Assert.assertTrue(ExposureHttpError.isError(408));
        Assert.assertFalse(ExposureHttpError.isError(300));
    }
}