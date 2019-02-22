package com.redbeemedia.enigma.core.json;

import android.util.JsonReader;

import com.redbeemedia.enigma.core.error.UnexpectedError;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public abstract class JsonReaderResponseHandler extends InputStreamResponseHandler {
    @Override
    protected void onInputStream(InputStream inputStream) {
        try {
            onSuccess(new JsonReader(new InputStreamReader(inputStream, "utf-8")));
        } catch (UnsupportedEncodingException e) {
            onError(new UnexpectedError(e));
        }
    }


    protected abstract void onSuccess(JsonReader jsonReader);
}
