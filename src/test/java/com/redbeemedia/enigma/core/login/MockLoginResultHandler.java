package com.redbeemedia.enigma.core.login;

import com.redbeemedia.enigma.core.error.Error;
import com.redbeemedia.enigma.core.session.ISession;

public class MockLoginResultHandler implements ILoginResultHandler {
    @Override
    public void onSuccess(ISession session) {
        //Ignore
    }

    @Override
    public void onError(Error error) {
        //ignore
    }
}
