package com.redbeemedia.enigma.core.util;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlPath {
    private String url;

    public UrlPath(String url) {
        if(url == null) {
            throw new NullPointerException("url is null");
        }
        this.url = url;
    }

    public UrlPath(UrlPath baseUrl, String subPart) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(baseUrl.url);
        int leadingSlashes = 0;
        if(!baseUrl.url.contains("?") && !subPart.startsWith("?")) {
            while (stringBuilder.charAt(stringBuilder.length() - 1) == '/') {
                stringBuilder.setLength(stringBuilder.length() - 1);
            }
            stringBuilder.append("/");
            //TODO handle when subPart is only slashes
            while (leadingSlashes < subPart.length() && subPart.charAt(leadingSlashes) == '/') {
                leadingSlashes++;
            }
        }
        this.url = stringBuilder.append(subPart.substring(leadingSlashes)).toString();
    }

    public UrlPath append(String path) {
        return new UrlPath(this, path);
    }


    public URL toURL() throws MalformedURLException {
        return new URL(url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof UrlPath) && equalsOwn((UrlPath) obj);
    }

    private boolean equalsOwn(UrlPath obj) {
        return this.url.equals(obj.url);
    }
}
