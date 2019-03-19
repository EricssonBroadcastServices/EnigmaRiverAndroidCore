package com.redbeemedia.enigma.core.http;

import java.io.IOException;
import java.io.OutputStream;

public interface IHttpCall {
    void prepare(IHttpConnection connection);
    String getRequestMethod();
    void writeBodyTo(OutputStream outputStream) throws IOException;
}
