package com.redbeemedia.enigma.core.entitlement;

import com.redbeemedia.enigma.core.error.Error;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IEntitlementResponseHandler {
    void onResponse(EntitlementData entitlementData);
    void onError(Error error);
}
