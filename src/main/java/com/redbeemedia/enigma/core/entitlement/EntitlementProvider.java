// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.entitlement;

import com.redbeemedia.enigma.core.error.EmptyResponseError;
import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.JsonResponseError;
import com.redbeemedia.enigma.core.error.ServerError;
import com.redbeemedia.enigma.core.error.UnexpectedError;
import com.redbeemedia.enigma.core.error.UnexpectedHttpStatusError;
import com.redbeemedia.enigma.core.http.HttpStatus;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.json.JsonInputStreamParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * <h3>NOTE</h3>
 * <p>This class is not part of the public API.</p>
 */
public class EntitlementProvider implements IEntitlementProvider {
    private final IHttpHandler httpHandler;

    public EntitlementProvider(IHttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    @Override
    public void checkEntitlement(IEntitlementRequest entitlementRequest, IEntitlementResponseHandler responseHandler) {
        try {
            entitlementRequest.doHttpCall(httpHandler, new HttpResponseHandler() {
                @Override
                protected void onStatus(String status) {
                    EntitlementStatus entitlementStatus;
                    try {
                        entitlementStatus = EntitlementStatus.valueOf(status);
                    } catch (IllegalArgumentException e) {
                        entitlementStatus = null;
                    }
                    responseHandler.onResponse(new EntitlementData(entitlementStatus));
                }

                @Override
                protected void onError(EnigmaError error) {
                    responseHandler.onError(new ServerError(error));
                }
            });
        } catch (MalformedURLException e) {
            responseHandler.onError(new UnexpectedError(e));
        }
    }

    private static abstract class HttpResponseHandler implements IHttpHandler.IHttpResponseHandler {
        @Override
        public void onResponse(HttpStatus httpStatus) {
            onError(new EmptyResponseError("Expected a response."));
        }

        @Override
        public void onResponse(HttpStatus httpStatus, InputStream inputStream) {
            try {
                int responseCode = httpStatus.getResponseCode();
                if(responseCode == 200) {
                    JSONObject jsonObject = JsonInputStreamParser.obtain().parse(inputStream);
                    onStatus(jsonObject.getString("status"));
                    return;
                } else if(responseCode == 403) {
                    JSONObject jsonObject = JsonInputStreamParser.obtain().parse(inputStream);
                    onStatus(jsonObject.getString("message"));
                    return;
                } else {
                    onError(new UnexpectedHttpStatusError(httpStatus));
                    return;
                }
            } catch (Exception e) {
                onException(e);
            }
        }

        @Override
        public void onException(Exception e) {
            if(e instanceof JSONException) {
                onError(new JsonResponseError(new UnexpectedError(e)));
            } else {
                onError(new UnexpectedError(e));
            }
        }

        protected abstract void onStatus(String status);

        protected abstract void onError(EnigmaError error);
    }
}
