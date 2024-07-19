// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.entitlement.listener;

import com.redbeemedia.enigma.core.entitlement.EntitlementData;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public class BaseEntitlementListener implements IEntitlementListener {
    @Override
    public void onEntitlementChanged(EntitlementData oldData, EntitlementData newData) {
    }
}
