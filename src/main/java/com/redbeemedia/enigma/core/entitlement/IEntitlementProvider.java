package com.redbeemedia.enigma.core.entitlement;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IEntitlementProvider {
    void checkEntitlement(IEntitlementRequest entitlementRequest, IEntitlementResponseHandler responseHandler);
}