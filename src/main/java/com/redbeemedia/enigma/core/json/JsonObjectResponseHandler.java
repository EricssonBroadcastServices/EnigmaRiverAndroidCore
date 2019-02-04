package com.redbeemedia.enigma.core.json;

import com.redbeemedia.enigma.core.error.Error;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

public abstract class JsonObjectResponseHandler extends InputStreamResponseHandler {
    private JsonInputStreamParser inputStreamParser = JsonInputStreamParser.obtain();

    @Override
    protected void onInputStream(InputStream inputStream) throws JSONException{
        onSuccess(inputStreamParser.parse(inputStream));
    }

    protected abstract void onSuccess(JSONObject jsonObject) throws JSONException;

    @Override
    public void onException(Exception e) {
        if(e instanceof JSONException) {
            onError(Error.FAILED_TO_PARSE_RESPONSE_JSON);
        } else {
            super.onException(e);
        }
    }
}
