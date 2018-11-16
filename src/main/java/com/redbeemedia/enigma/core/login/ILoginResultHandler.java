package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.session.ISession;

public interface ILoginResultHandler {
    void onSuccess(ISession session);
    void onError(Error error);
}
