// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.player.timeline;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

public class EnigmaEventMessage extends EnigmaMetadata.Entry {
    public final String schemeIdUri;
    public final String value;
    public final long durationMs;
    public final long id;
    public final byte[] messageData;
    private int hashCode = 0;

    public EnigmaEventMessage(EnigmaMetadata.Format format, byte[] bytes,
                              String schemeIdUri, String value, long durationMs, long id, byte[] messageData) {
        super(format, bytes);
        this.schemeIdUri = schemeIdUri;
        this.value = value;
        this.durationMs = durationMs;
        this.id = id;
        this.messageData = messageData;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            int result = 17;
            result = 31 * result + (schemeIdUri != null ? schemeIdUri.hashCode() : 0);
            result = 31 * result + (value != null ? value.hashCode() : 0);
            result = 31 * result + (int) (durationMs ^ (durationMs >>> 32));
            result = 31 * result + (int) (id ^ (id >>> 32));
            result = 31 * result + Arrays.hashCode(messageData);
            hashCode = result;
        }
        return hashCode;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        EnigmaEventMessage other = (EnigmaEventMessage) obj;
        return durationMs == other.durationMs
                && id == other.id
                && Objects.equals(schemeIdUri, other.schemeIdUri)
                && Objects.equals(value, other.value)
                && Arrays.equals(messageData, other.messageData);
    }

    @NonNull
    @Override
    public String toString() {
        return "EMSG: scheme="
                + schemeIdUri
                + ", id="
                + id
                + ", durationMs="
                + durationMs
                + ", value="
                + value;
    }
}
