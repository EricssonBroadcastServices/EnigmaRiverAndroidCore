// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.json;

import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.error.UnexpectedHttpStatusError;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.util.error.EnigmaErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Reads the entire response into a <code>String</code> if successful.
 */
public abstract class StringResponseHandler implements IHttpHandler.IHttpResponseHandler {

    @Override
    public void onResponse(HttpStatus httpStatus) {
        if (httpStatus == null || !(httpStatus.getResponseCode() == 200 || httpStatus.getResponseCode() == 204)) {
            onError(new UnexpectedHttpStatusError(httpStatus));
            return;
        }
        onSuccess("");
    }

    @Override
    public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
        if (httpStatus.getResponseCode() == 204) {
            onSuccess("");
            return;
        }
        if (httpStatus == null || httpStatus.getResponseCode() != 200) {
            onError(new UnexpectedHttpStatusError(httpStatus));
            return;
        }
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(inputStream, getCharSet());
        int charsRead;
        try {
            while((charsRead = in.read(buffer, 0, buffer.length)) > 0) {
                out.append(buffer, 0, charsRead);
            }
            onSuccess(out.toString());
        } catch (IOException e) {
            onError(new UnexpectedError(e));
        }
    }

    @Override
    public void onException(Exception e) {
        onError(new UnexpectedError(e));
    }

    public Charset getCharSet() { return StandardCharsets.UTF_8; }

    public abstract void onError(EnigmaError error);

    public abstract void onSuccess(String response);
}
