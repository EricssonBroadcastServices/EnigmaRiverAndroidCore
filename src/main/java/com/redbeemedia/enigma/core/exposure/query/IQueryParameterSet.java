package com.redbeemedia.enigma.core.exposure.query;

import com.redbeemedia.enigma.core.util.UrlPath;

public interface IQueryParameterSet {
    void add(IQueryParameter<?> queryParameter);
    UrlPath applyAll(UrlPath urlPath);
}
