package com.redbeemedia.enigma.core.json;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

//Note this class has nothing to do with json anymore! We should make it nicer and put it in an other package. When we need it elsewhere.
/*package-protected*/ abstract class InputStreamResponseHandler implements IHttpHandler.IHttpResponseHandler {
    private boolean allowEmptyResponse = false;
    private Map<Integer, IHttpCodeHandler> codeActions = new HashMap<>();

    public InputStreamResponseHandler() {
        codeActions.put(HttpURLConnection.HTTP_OK, new IHttpCodeHandler() {
            @Override
            public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
                try {
                    onInputStream(inputStream);
                } catch (Exception e) {
                    onException(e);
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
            onError(Error.UNEXPECTED_ERROR);
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

    protected InputStreamResponseHandler handleErrorCode(int httpCode, final Error error) {
        return handleErrorCode(httpCode, new IHttpCodeHandler() {
            @Override
            public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
                onError(error);
            }
        });
    }

    protected InputStreamResponseHandler handleErrorCode(int httpCode, IHttpCodeHandler httpCodeHandler) {
        codeActions.put(httpCode, httpCodeHandler);
        return this;
    }

    protected InputStreamResponseHandler allowEmptyResponse() {
        this.allowEmptyResponse = true;
        return this;
    }

    protected abstract void onInputStream(InputStream inputStream) throws Exception;

    protected abstract void onError(Error error);

    @Override
    public void onException(Exception e) {
        onError(Error.UNEXPECTED_ERROR);
    }

    protected interface IHttpCodeHandler {
        void onResponse(HttpStatus httpStatus, InputStream inputStream);
    }
}
