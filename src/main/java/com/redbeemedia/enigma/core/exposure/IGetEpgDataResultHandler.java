package com.redbeemedia.enigma.core.exposure;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.exposure.models.channel.ApiChannelEPGResponse;

import java.util.List;

public interface IGetEpgDataResultHandler {
    void onSuccess(List<ApiChannelEPGResponse> apiChannelEPGResponses);
    void onError(Error error);
}
