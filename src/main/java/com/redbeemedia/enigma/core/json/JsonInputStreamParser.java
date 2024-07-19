// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class JsonInputStreamParser {
    private static JsonInputStreamParser instance = new JsonInputStreamParser();

    private JsonInputStreamParser() {
    }

    public JSONObject parse(InputStream inputStream) throws JSONException {
        StringBuilder strResponse = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                strResponse.append(line);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        try {
            return new JSONObject(strResponse.toString());
        } catch (JSONException firstException) {
            try {
                JSONArray array = new JSONArray(strResponse.toString());
                JSONObject container = new JSONObject();
                container.put("array", array);
                return container;
            } catch (JSONException otherException) {
                throw firstException;
            }
        }
    }

    public static JsonInputStreamParser obtain() {
        return instance;
    }
}
