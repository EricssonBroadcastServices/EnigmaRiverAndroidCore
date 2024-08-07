// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import java.util.Map;

/**
 * Represents a set of key-value pairs to be used for ad insertion typically during a play request.
 */
public interface IAdInsertionParameters {

    /**
     * @return a list of key-value pairs used for ad insertion.
     */
    Map<String,?> getParameters();

}
