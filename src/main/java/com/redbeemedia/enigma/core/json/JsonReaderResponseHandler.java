package com.redbeemedia.enigma.core.json;

import android.util.JsonReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public abstract class JsonReaderResponseHandler extends InputStreamResponseHandler {
    @Override
    protected void onInputStream(InputStream inputStream) {
        onSuccess(new JsonReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)));
    }


    protected abstract void onSuccess(JsonReader jsonReader);
}
