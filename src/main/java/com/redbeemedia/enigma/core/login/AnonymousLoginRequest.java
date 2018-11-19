package com.redbeemedia.enigma.core.login;

import java.io.OutputStream;

public class AnonymousLoginRequest extends AbstractLoginRequest implements ILoginRequest {
    public AnonymousLoginRequest(ILoginResultHandler resultHandler) {
        super("auth/anonymous", "POST", resultHandler);
    }

    @Override
    public void writeBodyTo(OutputStream outputStream) {
        //TODO write deviceId and device info (at least 'type')
        /**
         * Example:
         * {
         *   "deviceId": "matte-testar",
         *   "device": {
         *     "type": "MOBILE"
         *   }
         * }
         */
    }
}
