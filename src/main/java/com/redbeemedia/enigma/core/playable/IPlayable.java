// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.playable;

import android.os.Parcelable;

public interface IPlayable extends Parcelable {
    void useWith(IPlayableHandler playableHandler);
}
