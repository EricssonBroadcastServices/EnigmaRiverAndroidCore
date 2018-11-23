package com.redbeemedia.enigma.core;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlPlayable implements IPlayable {
    private URL url;

    public UrlPlayable(String url) throws MalformedURLException {
        this(new URL(url));
    }

    public UrlPlayable(URL url) {
        this.url = url;
    }

    @Override
    public void useWith(IPlayableHandler playableHandler) {
        playableHandler.startUsingUrl(url);
    }
}
