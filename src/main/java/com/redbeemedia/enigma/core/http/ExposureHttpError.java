package com.redbeemedia.enigma.core.http;

import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class ExposureHttpError {
    private int httpCode;
    private String message;

    public ExposureHttpError(JSONObject errorJSONObject) throws JSONException {
        this.httpCode = errorJSONObject.getInt("httpCode");
        this.message = errorJSONObject.optString("message");
    }

    public int getHttpCode() {
        return httpCode;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return httpCode+" "+message;
    }

    public static boolean isError(int statusCode) {
        return statusCode >= 400 && statusCode <= 500;
    }
}