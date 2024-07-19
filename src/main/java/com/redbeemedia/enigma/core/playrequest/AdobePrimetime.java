// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.playrequest;

public class AdobePrimetime {

    public static final String HTTP_HEADER_KEY = "X-Adobe-Primetime-MediaToken";

    public final String token;

    /**
     * Base64 encoded string provided in <i>X-Adobe-Primetime-MediaToken</i>.
     * @param token the Base64 encoded token object.
     */
    public AdobePrimetime(String token) {
        this.token = token;
    }

}
