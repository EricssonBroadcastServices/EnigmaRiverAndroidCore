package com.redbeemedia.enigma.core.json;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonInputStreamParser {
    public JSONObject parse(InputStream inputStream) throws JSONException {
        StringBuilder strResponse = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                strResponse.append(line);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return new JSONObject(strResponse.toString());
    }
}
