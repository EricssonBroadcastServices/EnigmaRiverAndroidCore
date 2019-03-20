package com.redbeemedia.enigma.core.session;

import android.os.Parcel;
import android.os.Parcelable;

import com.redbeemedia.enigma.core.businessunit.BusinessUnit;
import com.redbeemedia.enigma.core.businessunit.IBusinessUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Session implements ISession {
    private static Map<UUID, Session> sessionMap = new HashMap<>();

    private final UUID id;
    private String sessionToken;
    private final IBusinessUnit businessUnit;

    public Session(String sessionToken, String customerUnit, String businessUnit) {
        this.id = UUID.randomUUID();
        this.sessionToken = sessionToken;
        this.businessUnit = new BusinessUnit(customerUnit, businessUnit);
        sessionMap.put(this.id, this);
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
            UUID sessionId = (UUID) in.readSerializable();
            return sessionMap.get(sessionId);
        }

        public Session[] newArray(int size) {
            return new Session[size];
        }
    };


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Session && this.id.equals(((Session) obj).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}