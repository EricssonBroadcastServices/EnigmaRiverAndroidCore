package com.redbeemedia.enigma.core.http;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.OutputStream;

public interface IHttpCall {
    void prepare(@NonNull IHttpConnection connection);
    String getRequestMethod();
    void writeBodyTo(@NonNull OutputStream outputStream) throws IOException;
}
