package com.redbeemedia.enigma.core.asset;

import android.util.JsonReader;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface JsonMapper<Type> {
    public Type create();
    public void map(final JsonReader jsonReader, final Type type, final int position) throws IOException;
    public Type onMapComplete(final Type type, final int position);

    public static class Util {

        public List<String> mapStrings(final JsonReader jsonReader) throws IOException {
            final ArrayList<String> strings = new ArrayList<>();
            jsonReader.beginArray();
            while (jsonReader.hasNext()) {
                strings.add(jsonReader.nextString());
            }
            jsonReader.endArray();

            strings.trimToSize();

            return strings;
        }

        public <Type extends Map<String, String>> Type mapKeyValue(final JsonReader jsonReader,
                                                                   final Type map)
                throws IOException
        {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                final String key = jsonReader.nextName();
                final String value = jsonReader.nextString();

                map.put(key, value);
            }
            jsonReader.endObject();

            return map;
        }

        public <Type> Type mapObject(final JsonReader jsonReader, final JsonMapper<Type> mapper) throws IOException {
            return mapObject(jsonReader, mapper, 0);
        }

        public <Type> Type mapObject(final JsonReader jsonReader, final JsonMapper<Type> mapper, final int position) throws IOException {
            final Type type = mapper.create();

            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                mapper.map(jsonReader, type, position);
            }
            jsonReader.endObject();
            return mapper.onMapComplete(type, position);
        }

        public <Type> ArrayList<Type> flatMapList(final JsonReader jsonReader,
                                                  final JsonMapper<Type> mapper)
                throws IOException
        {
            final ArrayList<Type> list = flatMapList(new ArrayList<Type>(), jsonReader, mapper);
            list.trimToSize();
            return list;
        }

        public <Type, ListType extends List<Type>> ListType flatMapList(final ListType list,
                                                                        final JsonReader jsonReader,
                                                                        final JsonMapper<Type> mapper)
                throws IOException
        {
            jsonReader.beginArray();
            for (int i = 0; jsonReader.hasNext(); i++) {
                final Type object = mapObject(jsonReader, mapper, i);

                if (object != null) {
                    list.add(object);
                }
            }
            jsonReader.endArray();

            return list;
        }

        public <Type> ArrayList<Type> mapList(final JsonReader jsonReader, final JsonMapper<Type> mapper) throws IOException {
            final ArrayList<Type> list = mapList(new ArrayList<Type>(), jsonReader, mapper);
            list.trimToSize();
            return list;
        }

        public <Type, ListType extends List<Type>> ListType mapList(final ListType list, final JsonReader jsonReader, final JsonMapper<Type> mapper) throws IOException {
            jsonReader.beginArray();
            for (int i = 0; jsonReader.hasNext(); i++) {
                list.add(mapObject(jsonReader, mapper, i));
            }
            jsonReader.endArray();

            return list;
        }

        public <Type> void mapArray(final JsonReader jsonReader, final JsonMapper<Type> mapper, final int offset) throws IOException {
            jsonReader.beginArray();
            for (int i = offset; jsonReader.hasNext(); i++) {
                mapObject(jsonReader, mapper, i);
            }
            jsonReader.endArray();
        }

        public <Type> Type[] createArray(final int expectedSize,
                                         final Class<Type> typeClass,
                                         final JsonReader jsonReader,
                                         final JsonMapper<Type> mapper)
                throws IOException
        {
            final Type[] items = (Type[]) Array.newInstance(typeClass, expectedSize);

            final int count = mapArray(items, jsonReader, mapper);

            final Type[] nonEmptyArray;
            if (count == expectedSize) {
                nonEmptyArray = items;
            } else {
                nonEmptyArray = (Type[]) Array.newInstance(typeClass, count);
                System.arraycopy(items, 0, nonEmptyArray, 0, count);
            }

            return nonEmptyArray;
        }

        public <Type> int mapArray(final Type[] array,
                                   final JsonReader jsonReader,
                                   final JsonMapper<Type> mapper)
                throws IOException
        {
            return mapArray(array, jsonReader, mapper, 0);
        }

        public <Type> int mapArray(final Type[] array,
                                      final JsonReader jsonReader,
                                      final JsonMapper<Type> mapper,
                                      final int offset)
                throws IOException
        {
            int count = 0;

            jsonReader.beginArray();
            for (int i = offset; jsonReader.hasNext(); i++) {
                count++;
                final Type type = mapObject(jsonReader, mapper, i);
                array[i] = type;
            }
            jsonReader.endArray();

            return count;
        }
    }
}
