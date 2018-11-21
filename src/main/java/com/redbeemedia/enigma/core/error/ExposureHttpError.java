package com.redbeemedia.enigma.core.error;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class ExposureHttpError {
    private int httpCode;
    private String message;

    ExposureHttpError(int httpCode, String message) {
        this.httpCode = httpCode;
        this.message = message;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public String getMessage() {
        return message;
    }

    public static ExposureHttpError getHttpError(JSONObject errorJSONObject) {
        return new ExposureHttpError(errorJSONObject.optInt("httpCode"), errorJSONObject.optString("message"));
    }

    //TODO: change name
    public static boolean isError(int statusCode) {
        return statusCode >= HttpsURLConnection.HTTP_BAD_REQUEST && statusCode <= HttpsURLConnection.HTTP_INTERNAL_ERROR;
    }
}