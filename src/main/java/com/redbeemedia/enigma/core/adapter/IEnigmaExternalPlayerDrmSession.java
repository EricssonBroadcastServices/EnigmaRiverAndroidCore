/*
 * SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
 *
 * SPDX-License-Identifier: MIT
 */
package com.redbeemedia.enigma.core.adapter;

import java.util.Map;

public interface IEnigmaExternalPlayerDrmSession {
    String getLicenseUrl();

    Map<String, String> getLicenseRequestHeaders();
}
