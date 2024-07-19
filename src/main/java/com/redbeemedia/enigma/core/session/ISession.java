// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.session;

import android.os.Parcelable;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;

public interface ISession extends Parcelable {
    String getSessionToken();
    IBusinessUnit getBusinessUnit();
    String getUserId();
}
