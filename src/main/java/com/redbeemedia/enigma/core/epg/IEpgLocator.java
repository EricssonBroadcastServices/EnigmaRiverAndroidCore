// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.epg;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;

public interface IEpgLocator {
    IEpg getEpg(IBusinessUnit businessUnit);
}
