package com.redbeemedia.enigma.core.error;

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

    //TODO: change name
    public static boolean isError(int statusCode) {
        return statusCode >= HttpsURLConnection.HTTP_BAD_REQUEST && statusCode <= HttpsURLConnection.HTTP_INTERNAL_ERROR;
    }
}