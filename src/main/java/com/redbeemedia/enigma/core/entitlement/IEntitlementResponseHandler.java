package com.redbeemedia.enigma.core.entitlement;

import com.redbeemedia.enigma.core.error.EnigmaError;

/**
 * <h3>NOTE</h3>
 * <p>This interface is not part of the public API.</p>
 */
public interface IEntitlementResponseHandler {
    void onResponse(EntitlementData entitlementData);
    void onError(EnigmaError error);
}
