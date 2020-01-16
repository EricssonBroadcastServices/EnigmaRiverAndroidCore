package com.redbeemedia.enigma.core.http;

public interface IHttpConnection {
    void setHeader(String name, String value);
    void setDoOutput(boolean value);
    void setDoInput(boolean value);
}
