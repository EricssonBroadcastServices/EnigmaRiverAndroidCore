// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.format;

import java.util.Set;

public interface IMediaFormatSupportSpec {
    boolean supports(EnigmaMediaFormat enigmaMediaFormat);

    Set<EnigmaMediaFormat> getSupportedFormats();
}
