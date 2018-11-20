package com.redbeemedia.enigma.core.session;

import android.os.Parcel;
import android.os.Parcelable;

import org.junit.Assert;
import org.junit.Test;

public class SessionTest {
    @Test
    public void testSerialization() {
        Session session = new Session("sessionToken", "customer", "business");
        byte[] data = serializeParcelable(session);
        Session retrievedSession = deserializeParcelable(data, Session.CREATOR);
        Assert.assertSame(session, retrievedSession);
    }

    private static byte[] serializeParcelable(Parcelable parcelable) {
        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        return parcel.marshall();
    }

    private static <T extends Parcelable> T deserializeParcelable(byte[] data, Parcelable.Creator<T> creator) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(data, 0, data.length);
        return creator.createFromParcel(parcel);
    }
}
