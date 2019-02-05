package com.redbeemedia.enigma.core.exposure;

import com.redbeemedia.enigma.core.error.Error;

public interface IExposureResultHandler<T> {
    void onSuccess(T result);
    void onError(Error error);
}
