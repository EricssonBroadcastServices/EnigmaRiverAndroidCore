// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.ads;

import androidx.annotation.Nullable;

import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.error.UnexpectedHttpStatusError;
import com.redbeemedia.enigma.core.http.IHttpHandler;
import com.redbeemedia.enigma.core.http.SimpleHttpCall;
import com.redbeemedia.enigma.core.json.JsonObjectResponseHandler;
import com.redbeemedia.enigma.core.util.UrlPath;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;

/** Responsible of loading VAST metadata. */
class NowtilusHlsLiveResourceLoader implements IAdResourceLoader {

    private URL url;
    private final INowtilusParser parser;
    private final IHttpHandler httpHandler;

    NowtilusHlsLiveResourceLoader(IHttpHandler httpHandler, INowtilusParser parser) {
        this.httpHandler = httpHandler;
        this.parser = parser;
    }

    void setManifestUrl(String manifestUrl) {
        this.url = createUrl(manifestUrl);
    }

    /**
     * Will check the VAST url for metadata and report ad events if detected.
     * This will fetch the metadata from the manifest, parse it and call
     * impression links found in the document if the playback position is
     * within the bounds of an ad.
     */
    @Override
    public void load(IAdsResourceLoaderDelegate delegate) {

        int HTTP_STATUS_NO_CONTENT = 204;
        SimpleHttpCall apiCall = new SimpleHttpCall("GET");
        httpHandler.doHttp(url, apiCall, new JsonObjectResponseHandler() {
            @Override
            protected void onError(EnigmaError error) {
                if(!(error instanceof UnexpectedHttpStatusError && ((UnexpectedHttpStatusError)error).getHttpStatus().getResponseCode() == HTTP_STATUS_NO_CONTENT)) {
                    error.printStackTrace();
                }
            }

            @Override
            protected void onSuccess(JSONObject jsonObject) {
                try {
                    synchronized(this) {
                        if(jsonObject != null) {
                            delegate.onEntriesLoaded(parser.parseEntries(jsonObject));
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Nullable
    private URL createUrl(String manifestUrl) {
        try {
            return new UrlPath(manifestUrl).append("vast").toURL();
        } catch(MalformedURLException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
