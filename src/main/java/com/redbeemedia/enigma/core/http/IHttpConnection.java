// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.http;

public interface IHttpConnection {
    void setHeader(String name, String value);
    void setDoOutput(boolean value);
    void setDoInput(boolean value);
}
