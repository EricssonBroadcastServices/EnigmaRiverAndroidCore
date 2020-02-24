package com.redbeemedia.enigma.core.login;

import androidx.annotation.NonNull;

import com.redbeemedia.enigma.core.error.EnigmaError;
import com.redbeemedia.enigma.core.session.ISession;
import com.redbeemedia.enigma.core.util.IInternalCallbackObject;

public interface ILoginResultHandler extends IInternalCallbackObject {
    void onSuccess(@NonNull ISession session);
    void onError(@NonNull EnigmaError error);
}
