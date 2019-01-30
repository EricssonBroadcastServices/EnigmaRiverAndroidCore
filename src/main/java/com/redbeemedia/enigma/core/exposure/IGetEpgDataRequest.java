package com.redbeemedia.enigma.core.exposure;

import com.redbeemedia.enigma.core.util.UrlPath;

public interface IGetEpgDataRequest {
    UrlPath appendQueryParameters(UrlPath basePath);
    IGetEpgDataResultHandler getResultHandler();
}
