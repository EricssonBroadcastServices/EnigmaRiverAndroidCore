// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.json;

import com.redbeemedia.enigma.core.error.JsonResponseError;
import com.redbeemedia.enigma.core.error.UnexpectedError;

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
            onError(new JsonResponseError(new UnexpectedError(e)));
        } else {
            super.onException(e);
        }
    }
}
