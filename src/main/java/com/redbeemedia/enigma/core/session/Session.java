package com.redbeemedia.enigma.core.session;

import android.os.Parcel;
import android.os.Parcelable;

import com.redbeemedia.enigma.core.businessunit.BusinessUnit;
import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;

public class Session implements ISession {

    private String sessionToken;
    private final IBusinessUnit businessUnit;
    private final String userId;

    @Deprecated
    public Session(String sessionToken, String customerUnit, String businessUnit, String userId) {
        this(sessionToken, new BusinessUnit(customerUnit, businessUnit), userId);
    }

    public Session(String sessionToken, IBusinessUnit businessUnit, String userId) {
        this.sessionToken = sessionToken;
        this.businessUnit = businessUnit;
        this.userId = userId;
    }

    private Session(Parcel parcel) {
        this.sessionToken = parcel.readString();
        this.businessUnit = parcel.readParcelable(getClass().getClassLoader());
        this.userId = parcel.readString();
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getSessionToken() {
        return sessionToken;
    }

    @Override
    public IBusinessUnit getBusinessUnit() {
        return businessUnit;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Session> CREATOR = new Parcelable.Creator<Session>() {

        public Session createFromParcel(Parcel in) {
            return new Session(in);
        }

        public Session[] newArray(int size) {
            return new Session[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sessionToken);
        dest.writeParcelable(businessUnit, flags);
        dest.writeString(userId);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Session && this.sessionToken.equals(((Session) obj).sessionToken);
    }

    @Override
    public int hashCode() {
        return sessionToken.hashCode();
    }

}