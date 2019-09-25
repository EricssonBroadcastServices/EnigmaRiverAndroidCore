package com.redbeemedia.enigma.core.util;

import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class JsonReaderUtil {
    public static <T> List<T> readArray(JsonReader jsonReader, Class<T> type) throws IOException {
        try {
            IObjectFactory<T> factory = getDefaultFactory(type);
            return readArray(jsonReader, type, factory);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Type "+type.getName()+" does not have a constructor that takes JsonReader as it's single argument.",e);
        }
    }

    public static <T> List<T> readArray(JsonReader jsonReader, Class<T> type, IObjectFactory<T> factory) throws IOException {
        List<T> list = new ArrayList<>();
        jsonReader.beginArray();
        while(jsonReader.hasNext()) {
            T obj;
            try {
                obj = factory.newInstance(jsonReader);
            } catch (Exception e) {
                throw new IOException("Could not instantiate object using " + factory.getClass().getName() + ".", e);
            }
            list.add(obj);
        }
        jsonReader.endArray();
        return list;
    }

    private static <T> IObjectFactory<T> getDefaultFactory(Class<T> type) throws NoSuchMethodException {
        if(String.class.equals(type)) {
            return (IObjectFactory<T>) ObjectFactories.STRING;
        } else if(Boolean.class.equals(type)) {
            return (IObjectFactory<T>) ObjectFactories.BOOLEAN;
        } else if(Integer.class.equals(type)) {
            return (IObjectFactory<T>) ObjectFactories.INT;
        } else if(Double.class.equals(type)) {
            return (IObjectFactory<T>) ObjectFactories.DOUBLE;
        } else if(Long.class.equals(type)) {
            return (IObjectFactory<T>) ObjectFactories.LONG;
        } else {
            return new ObjectFactories.ConstructorFactory<T>(type);
        }
    }

    public interface IObjectFactory<T> {
        T newInstance(JsonReader jsonReader) throws Exception;
    }

    private static class ObjectFactories {
        private static final IObjectFactory<String> STRING = jsonReader -> jsonReader.nextString();
        private static final IObjectFactory<Boolean> BOOLEAN = jsonReader -> Boolean.valueOf(jsonReader.nextBoolean());
        private static final IObjectFactory<Integer> INT = jsonReader -> Integer.valueOf(jsonReader.nextInt());
        private static final IObjectFactory<Double> DOUBLE = jsonReader -> Double.valueOf(jsonReader.nextDouble());
        private static final IObjectFactory<Long> LONG = jsonReader -> Long.valueOf(jsonReader.nextLong());
        private static class ConstructorFactory<T> implements IObjectFactory<T> {
            private Constructor<T> constructor;

            public ConstructorFactory(Class<T> type) throws NoSuchMethodException {
                this.constructor = type.getConstructor(JsonReader.class);
            }

            @Override
            public T newInstance(JsonReader jsonReader) throws Exception {
                final boolean accessible = constructor.isAccessible();
                try {
                    constructor.setAccessible(true);
                    return constructor.newInstance(jsonReader);
                } finally {
                    try {
                        constructor.setAccessible(accessible);
                    } catch (Exception e) {/*ignore*/}
                }
            }
        }
    }

    public static final IObjectFactory<JSONObject> JSON_OBJECT_FACTORY = new IObjectFactory<JSONObject>() {
        @Override
        public JSONObject newInstance(JsonReader jsonReader) throws Exception {
            JSONObject jsonObject = new JSONObject();
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                jsonObject.put(jsonReader.nextName(), nextValue(jsonReader));
            }
            jsonReader.endObject();
            return jsonObject;
        }

        private Object nextValue(JsonReader jsonReader) throws Exception {
            switch (jsonReader.peek()) {
                case BEGIN_ARRAY: {
                    JSONArray jsonArray = new JSONArray();
                    for(Object arrayItem : JsonReaderUtil.readArray(jsonReader, Object.class, jsonReader1 -> nextValue(jsonReader1))) {
                        jsonArray.put(arrayItem);
                    }
                    return jsonArray;
                }
                case BEGIN_OBJECT: {
                    return JSON_OBJECT_FACTORY.newInstance(jsonReader);
                }
                case STRING: {
                    return jsonReader.nextString();
                }
                case NUMBER: {
                    return jsonReader.nextDouble();
                }
                case BOOLEAN: {
                    return jsonReader.nextBoolean();
                }
                case NULL: {
                    jsonReader.skipValue();
                    return null;
                }
            }
            throw new IllegalStateException();
        }
    };
}
