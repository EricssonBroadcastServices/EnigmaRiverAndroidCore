package com.redbeemedia.enigma.core.epg.response;

import androidx.annotation.NonNull;

import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.util.IInternalCallbackObject;

public interface IEpgResponseHandler extends IInternalCallbackObject {
    void onSuccess(@NonNull IEpgResponse epgResponse);
    void onError(@NonNull EnigmaError error);
}
