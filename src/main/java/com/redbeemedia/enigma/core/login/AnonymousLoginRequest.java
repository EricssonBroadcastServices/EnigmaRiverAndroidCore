package com.redbeemedia.enigma.core.login;

public class AnonymousLoginRequest extends AbstractLoginRequest implements ILoginRequest {
    public AnonymousLoginRequest() {
        super("auth/anonymous");
    }
}
