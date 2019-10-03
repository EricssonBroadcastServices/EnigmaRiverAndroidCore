package com.redbeemedia.enigma.core.entitlement;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public enum EntitlementStatus {
    SUCCESS,
    NOT_ENTITLED,
    GEO_BLOCKED,
    DOWNLOAD_BLOCKED,
    DEVICE_BLOCKED,
    LICENSE_EXPIRED,
    NOT_AVAILABLE_IN_FORMAT,
    CONCURRENT_STREAMS_LIMIT_REACHED,
    NOT_ENABLED,
    GAP_IN_EPG,
    EPG_PLAY_MAX_HOURS,
    ANONYMOUS_IP_BLOCKED;
}
