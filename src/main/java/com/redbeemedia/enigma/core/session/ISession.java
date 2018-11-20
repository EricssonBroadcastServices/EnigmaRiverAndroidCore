package com.redbeemedia.enigma.core.session;

import android.os.Parcelable;

public interface ISession extends Parcelable {
    String getSessionToken();
    String getCustomerUnitName();
    String getBusinessUnitName();
}
