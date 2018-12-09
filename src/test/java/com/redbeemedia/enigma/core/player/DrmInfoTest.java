package com.redbeemedia.enigma.core.player;

import org.junit.Assert;
import org.junit.Test;

public class DrmInfoTest {
    @Test
    public void testBaseUrl() {
        DrmInfo drmInfo = new DrmInfo("https://ericsson-t-mobilenl.stage.ott.irdeto.com/licenseServer/widevine/v1/Ericsson/license?contentId=fcd972f1-0405-44f6-adbc-e0dd29726c73_enigma&keyId=5f1bd8a5-2375-43ac-90ea-6d1d046b6067&ls_session=eyJBpZ6lxuOE&token=Bearer%20eyJ0eXAiOiJKV1QiLCJraWQiOiI3OGMzM2M3My1jNzZkLTQxMGMtYTZmNC05NmQ1NGNkN2VkMTIiLCJhbGciOiJIUzI1NiJ9-6lxusOE", "eyJ0eXAiOiJKV1QiLCJraWQiOiI3OGMzM2M3My1jNzZkLTQxMGMtYTZmNC05NmQ1NGNkN2VkMTIiLCJhbGciOiJIUzI1NiJ9");
        String[]expected = {"Authorization","Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiI3OGMzM2M3My1jNzZkLTQxMGMtYTZmNC05NmQ1NGNkN2VkMTIiLCJhbGciOiJIUzI1NiJ9"};
        Assert.assertArrayEquals(expected,drmInfo.getDrmKeyRequestPropertiesArray());
    }
}