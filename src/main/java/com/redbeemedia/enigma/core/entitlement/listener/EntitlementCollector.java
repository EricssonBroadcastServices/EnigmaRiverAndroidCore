// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.entitlement.listener;

import com.redbeemedia.enigma.core.entitlement.EntitlementData;
import com.redbeemedia.enigma.core.util.Collector;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public class EntitlementCollector extends Collector<IEntitlementListener> implements IEntitlementListener {
    public EntitlementCollector() {
        super(IEntitlementListener.class);
    }

    @Override
    public void onEntitlementChanged(EntitlementData oldData, EntitlementData newData) {
        forEach(listener -> listener.onEntitlementChanged(oldData, newData));
    }
}
