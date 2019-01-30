package com.redbeemedia.enigma.core.util;

import android.util.JsonReader;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class JsonReaderUtil {
    private static final int CATEGORY_STRING = 1;
    public static <T> List<T> readArray(JsonReader jsonReader, Class<T> type) throws IOException {
        try {
            int category = 0;
            Constructor<T> typeConstructor = null;
            if(String.class.equals(type)) {
                category = CATEGORY_STRING;
            } else {
                typeConstructor = type.getConstructor(JsonReader.class);
            }
            List<T> list = new ArrayList<>();
            jsonReader.beginArray();
            while(jsonReader.hasNext()) {
                T obj;
                if(category == CATEGORY_STRING) {
                    obj = (T) jsonReader.nextString();
                } else {
                    try {
                        obj = typeConstructor.newInstance(jsonReader);
                    } catch (Exception e) {
                        throw new RuntimeException("Could not instantiate object using "+typeConstructor.getName()+".", e);
                    }
                }
                list.add(obj);
            }
            jsonReader.endArray();
            return list;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Type "+type.getName()+" does not have a constructor that takes JsonReader as it's single argument.",e);
        }
    }
}
