package com.redbeemedia.enigma.core.http;

import java.io.OutputStream;

//TODO have this class? Unnessecary complexity?
public interface IHttpPreparator {
    void prepare(IHttpConnection connection);
    void writeBodyTo(OutputStream outputStream);
}
