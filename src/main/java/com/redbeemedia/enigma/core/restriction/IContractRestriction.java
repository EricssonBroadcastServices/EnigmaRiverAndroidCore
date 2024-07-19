// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.restriction;

public interface IContractRestriction<T> {
    T getValue(IContractRestrictionsValueSource valueSource);
}
