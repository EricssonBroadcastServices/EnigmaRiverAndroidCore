package com.redbeemedia.enigma.core.json;

import android.util.JsonReader;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public abstract class JsonReaderResponseHandler implements IHttpHandler.IHttpResponseHandler {
    private boolean allowEmptyResponse = false;
    private Map<Integer, IHttpCodeHandler> codeActions = new HashMap<>();

    public JsonReaderResponseHandler() {
        codeActions.put(HttpURLConnection.HTTP_OK, new IHttpCodeHandler() {
            @Override
            public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
                try {
                    onSuccess(new JsonReader(new InputStreamReader(inputStream, "utf-8")));
                } catch (UnsupportedEncodingException e) {
                    onException(e); //utf-8 is not supported. The world is doomed!
                }
            }
        });
    }

    @Override
    public void onResponse(HttpStatus status, InputStream inputStream) {
        IHttpCodeHandler httpCodeHandler = codeActions.get(status.getResponseCode());
        if(httpCodeHandler != null) {
            httpCodeHandler.onResponse(status, inputStream);
        } else {
            onError(Error.NETWORK_ERROR);
        }
    }

    @Override
    public void onResponse(HttpStatus httpStatus) {
        if(allowEmptyResponse) {
            onResponse(httpStatus, new ByteArrayInputStream(new byte[0]));
        } else {
            onError(Error.EMPTY_RESPONSE);
        }
    }

    protected JsonReaderResponseHandler handleErrorCode(int httpCode, final Error error) {
        return handleErrorCode(httpCode, new IHttpCodeHandler() {
            @Override
            public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
                onError(error);
            }
        });
    }

    protected JsonReaderResponseHandler handleErrorCode(int httpCode, IHttpCodeHandler httpCodeHandler) {
        codeActions.put(httpCode, httpCodeHandler);
        return this;
    }

    protected JsonReaderResponseHandler allowEmptyResponse() {
        this.allowEmptyResponse = true;
        return this;
    }



    protected abstract void onError(Error error);

    protected abstract void onSuccess(JsonReader jsonReader);

    @Override
    public void onException(Exception e) {
        onError(Error.UNEXPECTED_ERROR);
    }

    protected interface IHttpCodeHandler {
        void onResponse(HttpStatus httpStatus, InputStream inputStream);
    }
}
