// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.playable;

/**
 * A Playable originating from an asset.
 */
public interface IAssetPlayable extends IPlayable {
    String getAssetId();
}
