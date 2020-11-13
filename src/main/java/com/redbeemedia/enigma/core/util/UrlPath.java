package com.redbeemedia.enigma.core.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlPath implements IStringAppendable {
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

    @Override
    public UrlPath append(String path) {
        return new UrlPath(this, path);
    }

    public UrlPath appendQueryStringParameters(Map<String, ?> parameters) {
        UrlPath self = this;
        Pattern rightMostQuestionMark = Pattern.compile("\\/.*\\?");
        if (!rightMostQuestionMark.matcher(url).find()) {
            self = self.append("?");
        }

        for (Map.Entry<String, ?> kvp : parameters.entrySet()) {
            if (kvp.getValue() != null) {
                if (!self.url.endsWith("?")) {
                    self = self.append("&");
                }
                self = self.append(kvp.getKey() + "=" + kvp.getValue());
            }
        }
        return self;
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

    public boolean contains(String str) {
        return url.contains(str);
    }

    @Override
    public String toString() {
        return url;
    }
}
