package com.redbeemedia.enigma.core.entitlement;

import java.util.Objects;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public final class EntitlementData {
    private final EntitlementStatus status;

    public EntitlementData(EntitlementStatus status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return status == EntitlementStatus.SUCCESS;
    }

    public EntitlementStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EntitlementData && Objects.equals(((EntitlementData) obj).status, this.status);
    }

    @Override
    public int hashCode() {
        return status == null ? 0 : status.hashCode();
    }
}
