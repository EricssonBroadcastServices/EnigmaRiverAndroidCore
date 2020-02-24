package com.redbeemedia.enigma.core.epg.response;

import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.util.IInternalCallbackObject;

public interface IEpgResponseHandler extends IInternalCallbackObject {
    void onSuccess(IEpgResponse epgResponse);
    void onError(EnigmaError error);
}
