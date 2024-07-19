// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import java.net.URL;
import java.util.Collection;

/**
 * Represents a set of URLs with their corresponding impression event.
 */
class VastImpression {

    private final Collection<URL> urls;

    /** The impression event type of the stream. */
    final AdEventType type;
    /** If set to `true`, URLs in this impression event list is considered to be sent. */
    private boolean sent;

    VastImpression(AdEventType type, Collection<URL> urls) {
        this.type = type;
        this.urls = urls;
    }

    /** Call this method once the impression URLs has been sent. */
    void setSent() {
        sent = true;
    }

    /**
     * @return true if the send flag has been set.
     */
    boolean isSent() { return sent; }

    /**
     * @return All URLs to be triggered for this event.
     */
    Collection<URL> getUrls() { return urls; }
}
