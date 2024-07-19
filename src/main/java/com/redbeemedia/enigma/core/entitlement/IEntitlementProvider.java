// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.entitlement;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IEntitlementProvider {
    void checkEntitlement(IEntitlementRequest entitlementRequest, IEntitlementResponseHandler responseHandler);
}