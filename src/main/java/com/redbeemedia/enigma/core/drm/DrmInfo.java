// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.drm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*package-protected*/ class DrmInfo implements IDrmInfo {
    private String licenseUrl;
    private Iterable<Map.Entry<String,String>> drmKeyRequestProperties;

    public DrmInfo(String licenseUrl, String playToken) {
        this(licenseUrl, playToken, null);
    }

    public DrmInfo(String licenseUrl, String playToken, String requestId) {
        this.licenseUrl = licenseUrl;
        List<Map.Entry<String,String>> headers = new ArrayList<>();
        if(playToken != null) {
            headers.add(new KeyValuePair("Authorization", "Bearer " + playToken));
        }
        if(requestId != null) {
            headers.add(new KeyValuePair("X-Request-Id", requestId));
        }
        this.drmKeyRequestProperties = headers;
    }

    @Override
    public String getLicenseUrl() {
        return licenseUrl;
    }

    @Override
    public Iterable<Map.Entry<String, String>> getDrmKeyRequestProperties() {
        return drmKeyRequestProperties;
    }

    private static class KeyValuePair implements Map.Entry<String,String> {
        private final String key;
        private final String value;

        public KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(String value) {
            throw new UnsupportedOperationException();
        }
    }
}