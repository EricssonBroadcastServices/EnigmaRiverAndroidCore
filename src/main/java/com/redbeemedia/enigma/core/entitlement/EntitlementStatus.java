package com.redbeemedia.enigma.core.entitlement;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public enum EntitlementStatus {
    SUCCESS,
    FORBIDDEN, //If this business unit has been configured to require server to server authentication, but it is not valid.
    NOT_AVAILABLE, //The asset is not available (playable) even if the asset itself is known.
    BLOCKED, //All play requests for the asset is currently blocked. (for instance blacked out or catchup blocked)
    GEO_BLOCKED, //Play is not allowed in selected region.
    CONCURRENT_STREAMS_LIMIT_REACHED, //Play is not allowed due to concurrent streams limitation.
    NOT_PUBLISHED, //The asset is not published.
    NOT_ENTITLED; //The user does not have access to play the asset.
}
