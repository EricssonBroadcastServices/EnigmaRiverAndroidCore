package com.redbeemedia.enigma.core.util;

import android.util.JsonReader;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class JsonReaderUtil {
    private static final int CATEGORY_DEFAULT = 0;
    private static final int CATEGORY_STRING = 1;
    private static final int CATEGORY_BOOLEAN = 2;
    private static final int CATEGORY_INT = 3;
    private static final int CATEGORY_DOUBLE = 4;
    private static final int CATEGORY_LONG = 5;

    public static <T> List<T> readArray(JsonReader jsonReader, Class<T> type) throws IOException {
        try {
            int category = getCategory(type);
            Constructor<T> typeConstructor = null;
            if(category == CATEGORY_DEFAULT) {
                typeConstructor = type.getConstructor(JsonReader.class);
            }
            List<T> list = new ArrayList<>();
            jsonReader.beginArray();
            while(jsonReader.hasNext()) {
                T obj;
                switch(category) {
                    case CATEGORY_STRING: {
                        obj = (T) jsonReader.nextString();
                    } break;
                    case CATEGORY_BOOLEAN: {
                        obj = (T) Boolean.valueOf(jsonReader.nextBoolean());
                    } break;
                    case CATEGORY_INT: {
                        obj = (T) Integer.valueOf(jsonReader.nextInt());
                    } break;
                    case  CATEGORY_DOUBLE: {
                        obj = (T) Double.valueOf(jsonReader.nextDouble());
                    } break;
                    case CATEGORY_LONG: {
                        obj = (T) Long.valueOf(jsonReader.nextLong());
                    } break;
                    default: {
                        try {
                            obj = typeConstructor.newInstance(jsonReader);
                        } catch (Exception e) {
                            throw new IOException("Could not instantiate object using " + typeConstructor.getName() + ".", e);
                        }
                    } break;
                }
                list.add(obj);
            }
            jsonReader.endArray();
            return list;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Type "+type.getName()+" does not have a constructor that takes JsonReader as it's single argument.",e);
        }
    }

    private static int getCategory(Class<?> type) {
        if(String.class.equals(type)) {
            return CATEGORY_STRING;
        } else if(Boolean.class.equals(type)) {
            return CATEGORY_BOOLEAN;
        } else if(Integer.class.equals(type)) {
            return CATEGORY_INT;
        } else if(Double.class.equals(type)) {
            return CATEGORY_DOUBLE;
        } else if(Long.class.equals(type)) {
            return CATEGORY_LONG;
        } else {
            return CATEGORY_DEFAULT;
        }
    }
}
