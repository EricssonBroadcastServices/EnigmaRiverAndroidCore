// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.entitlement.listener;

import com.redbeemedia.enigma.core.entitlement.EntitlementData;
import com.redbeemedia.enigma.core.util.IInternalListener;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IEntitlementListener extends IInternalListener {
    void onEntitlementChanged(EntitlementData oldData, EntitlementData newData);
}
