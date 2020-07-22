package com.redbeemedia.enigma.core.playrequest;

import com.redbeemedia.enigma.core.session.ISession;

public abstract class BasePlayRequest implements IPlayRequest {
    @Deprecated
    @Override
    public final void _dont_implement_IPlayRequest___instead_extend_BasePlayRequest_() {
    }

    @Override
    public ISession getSession() {
        return null;
    }
}
