package com.redbeemedia.enigma.core.json;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public abstract class JsonResponseHandler implements IHttpHandler.IHttpResponseHandler {
    private boolean allowEmptyResponse = false;
    private Map<Integer, IHttpCodeHandler> codeActions = new HashMap<>();

    public JsonResponseHandler() {
        codeActions.put(HttpURLConnection.HTTP_OK, new IHttpCodeHandler() {
            private JsonInputStreamParser inputStreamParser = JsonInputStreamParser.obtain();
            @Override
            public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
                try {
                    onSuccess(inputStreamParser.parse(inputStream));
                } catch (JSONException e) {
                    onError(Error.FAILED_TO_PARSE_RESPONSE_JSON);
                }
            }
        });
    }

    @Override
    public void onResponse(final HttpStatus status, InputStream inputStream) {
        IHttpCodeHandler httpCodeHandler = codeActions.get(status.code);
        if(httpCodeHandler != null) {
            httpCodeHandler.onResponse(status, inputStream);
        } else {
            onError(Error.TODO);
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

    protected JsonResponseHandler handleErrorCode(int httpCode, final Error error) {
        return handleErrorCode(httpCode, new IHttpCodeHandler() {
            @Override
            public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
                onError(error);
            }
        });
    }

    protected JsonResponseHandler handleErrorCode(int httpCode, IHttpCodeHandler httpCodeHandler) {
        codeActions.put(httpCode, httpCodeHandler);
        return this;
    }

    protected JsonResponseHandler allowEmptyResponse() {
        this.allowEmptyResponse = true;
        return this;
    }



    protected abstract void onError(Error error);

    protected abstract void onSuccess(JSONObject jsonObject) throws JSONException;

    @Override
    public void onException(Exception e) {
        throw new RuntimeException(e); //TODO
    }

    protected interface IHttpCodeHandler {
        void onResponse(HttpStatus httpStatus, InputStream inputStream);
    }
}
