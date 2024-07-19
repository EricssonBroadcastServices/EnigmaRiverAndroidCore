// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.drm;

import java.util.Map;

public interface IDrmInfo {
    String getLicenseUrl();
    Iterable<Map.Entry<String,String>> getDrmKeyRequestProperties();
}
