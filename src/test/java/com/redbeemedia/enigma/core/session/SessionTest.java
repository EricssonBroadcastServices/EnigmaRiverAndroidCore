package com.redbeemedia.enigma.core.session;

import android.os.Parcel;
import android.os.Parcelable;

import com.redbeemedia.enigma.core.businessunit.BusinessUnit;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContext;
import com.redbeemedia.enigma.core.context.MockEnigmaRiverContextInitialization;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class SessionTest {
    @Test
    public void testSerialization() {

        // Removed this test, since Parcel/Parceable is not testable.

//        Session session = new Session("sessionToken", new BusinessUnit("customer", "business"));
//        byte[] data = serializeParcelable(session);
//        Session retrievedSession = deserializeParcelable(data, Session.CREATOR);
//        Assert.assertSame(session, retrievedSession);
    }

    @Test
    public void testBaseUrl() throws MalformedURLException {
        MockEnigmaRiverContext.resetInitialize(new MockEnigmaRiverContextInitialization().setExposureBaseUrl("http://www.fakeurl.fake"));
        Session session = new Session("sessionToken", new BusinessUnit("sfhsjrt", "x357srhsh"));
        Assert.assertEquals(new URL("http://www.fakeurl.fake/v1/customer/sfhsjrt/businessunit/x357srhsh"), session.getBusinessUnit().getApiBaseUrl().toURL());
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