// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.restriction;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public interface IContractRestrictionsValueSource {
    <T> boolean hasValue(String name, Class<T> type);
    <T> T getValue(String name, Class<T> type);
}
