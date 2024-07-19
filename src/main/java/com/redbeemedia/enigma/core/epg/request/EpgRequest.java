// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.epg.request;

public class EpgRequest extends AbstractEpgRequest {
    public EpgRequest(String channelId, long fromUtcMillis, long toUtcMillis) {
        super(channelId, fromUtcMillis, toUtcMillis);
    }
}
