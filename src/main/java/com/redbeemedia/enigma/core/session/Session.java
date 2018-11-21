package com.redbeemedia.enigma.core.session;

import android.os.Parcel;
import android.os.Parcelable;

import com.redbeemedia.enigma.core.context.EnigmaRiverContext;
import com.redbeemedia.enigma.core.util.UrlPath;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Session implements ISession {
    private static Map<UUID, Session> sessionMap = new HashMap<>();

    private final UUID id;
    private String sessionToken;
    private String custumerUnit;
    private String businessUnit;

    public Session(String sessionToken, String custumerUnit, String businessUnit) {
        this.id = UUID.randomUUID();
        this.sessionToken = sessionToken;
        this.custumerUnit = custumerUnit;
        this.businessUnit = businessUnit;
        sessionMap.put(this.id, this);
    }

    @Override
    public String getSessionToken() {
        return sessionToken;
    }

    @Override
    public String getCustomerUnitName() {
        return custumerUnit;
    }

    @Override
    public String getBusinessUnitName() {
        return businessUnit;
    }

    @Override
    public UrlPath getApiBaseUrl() {
        return EnigmaRiverContext.getExposureBaseUrl().append("v1/customer").append(custumerUnit).append("businessunit").append(businessUnit);
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
}