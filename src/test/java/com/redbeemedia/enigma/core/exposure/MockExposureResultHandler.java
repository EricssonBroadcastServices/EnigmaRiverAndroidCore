package com.redbeemedia.enigma.core.exposure;

import com.redbeemedia.enigma.core.error.Error;

public class MockExposureResultHandler<T> implements IExposureResultHandler<T> {
    @Override
    public void onSuccess(T result) {
    }

    @Override
    public void onError(Error error) {
    }
}