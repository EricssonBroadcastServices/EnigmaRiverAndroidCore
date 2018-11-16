package com.redbeemedia.enigma.core.http;

import java.io.OutputStream;

public interface IHttpPreparator {
    void prepare(IHttpConnection connection);
    String getRequestMethod();
    void writeBodyTo(OutputStream outputStream);
}
