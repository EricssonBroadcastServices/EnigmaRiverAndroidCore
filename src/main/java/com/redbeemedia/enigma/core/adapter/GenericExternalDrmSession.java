/*
 * SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
 *
 * SPDX-License-Identifier: MIT
 */
package com.redbeemedia.enigma.core.adapter;

import java.util.Collections;
import java.util.Map;

public class GenericExternalDrmSession implements IEnigmaExternalPlayerDrmSession {
    private final String licenseUrl;
    private final Map<String, String> requestHeaders;

    public GenericExternalDrmSession(String licenseUrl, Map<String, String> requestHeaders) {
        this.licenseUrl = licenseUrl;
        this.requestHeaders = requestHeaders != null ? requestHeaders : Collections.emptyMap();
    }

    @Override
    public String getLicenseUrl() {
        return licenseUrl;
    }

    @Override
    public Map<String, String> getLicenseRequestHeaders() {
        return Collections.unmodifiableMap(requestHeaders);
    }
}