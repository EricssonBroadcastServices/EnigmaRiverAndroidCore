package com.redbeemedia.enigma.core.session;

import android.os.Parcelable;

import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;

public interface ISession extends Parcelable {
    String getSessionToken();
    IBusinessUnit getBusinessUnit();
    String getUserId();
}
