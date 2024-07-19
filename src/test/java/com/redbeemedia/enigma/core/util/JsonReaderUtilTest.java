// SPDX-FileCopyrightText: 2024 Red Bee Media Ltd <https://www.redbeemedia.com/>
//
// SPDX-License-Identifier: MIT

package com.redbeemedia.enigma.core.util;

import android.util.JsonReader;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

public class JsonReaderUtilTest {
    @Test
    public void testReadStringList() throws IOException {
        List<String> list = JsonReaderUtil.readArray(new JsonReader(new StringReader("[\"one\",\"two\"]")), String.class);
        Assert.assertEquals(Arrays.asList("one","two"), list);
    }

    @Test
    public void testReadIntList() throws IOException {
        List<Integer> list = JsonReaderUtil.readArray(new JsonReader(new StringReader("[42,1337]")), Integer.class);
        Assert.assertEquals(Arrays.asList(42,1337), list);
    }

    @Test
    public void testReadLongList() throws IOException {
        List<Long> list = JsonReaderUtil.readArray(new JsonReader(new StringReader("[42,1337]")), Long.class);
        Assert.assertEquals(Arrays.asList(42L,1337L), list);
    }

    @Test
    public void testReadBooleanList() throws IOException {
        List<Boolean> list = JsonReaderUtil.readArray(new JsonReader(new StringReader("[true,false,true]")), Boolean.class);
        Assert.assertEquals(Arrays.asList(true,false,true), list);
    }

    @Test
    public void testReadDoubleList() throws IOException {
        List<Double> list = JsonReaderUtil.readArray(new JsonReader(new StringReader("[42.1337,1337.42]")), Double.class);
        Assert.assertEquals(Arrays.asList(42.1337,1337.42), list);
    }

    @Test
    public void testCustomValueCreator() throws IOException {
        List<Number> numbers = JsonReaderUtil.readArray(new JsonReader(new StringReader("[{\"type\": \"long\",\"value\": 4},{\"value\": 4,\"type\": \"float\"}]")), Number.class, new JsonReaderUtil.IObjectFactory<Number>() {
            @Override
            public Number newInstance(JsonReader jsonReader) throws Exception {
                class Model {
                    private String type;
                    private String value;

                    public Number createNumber() {
                        if("long".equals(type)) {
                            return Long.valueOf(value);
                        } else if("float".equals(type)) {
                            return Float.valueOf(value);
                        } else {
                          throw new RuntimeException(type);
                        }
                    }
                }
                Model model = new Model();
                jsonReader.beginObject();
                while(jsonReader.hasNext()) {
                    switch (jsonReader.nextName()) {
                        case "type": {
                            model.type = jsonReader.nextString();
                        } break;
                        default:
                            model.value = jsonReader.nextString();
                    }
                }
                jsonReader.endObject();
                return model.createNumber();
            }
        });
        Assert.assertEquals(2, numbers.size());
        Assert.assertTrue("Expected long", numbers.get(0) instanceof Long);
        Assert.assertTrue("Expected float", numbers.get(1) instanceof Float);
    }

    @Test
    public void testDefaultConstructor() throws IOException {
        List<ConformingType> list = JsonReaderUtil.readArray(new JsonReader(new StringReader("[{\"name\": \"alpha\"},{\"name\": \"beta\"}]")),ConformingType.class);
        Assert.assertEquals(2, list.size());
        Assert.assertEquals("alpha", list.get(0).name);
        Assert.assertEquals("beta", list.get(1).name);
    }

    public void testReadJsonObject() throws Exception {
        JSONObject expected = new JSONObject();
        expected.put("number", 5);
        expected.put("string", "This is a string");
        expected.put("boolean", true);
        JSONArray intArray = new JSONArray();
        intArray.put(1);
        intArray.put(2);
        intArray.put(3);
        expected.put("intArray", intArray);
        JSONArray objectArray = new JSONArray();
        for(int i = 0; i < 5; ++i) {
            JSONObject arrayItem = new JSONObject();
            arrayItem.put("index", i);
            objectArray.put(arrayItem);
        }
        expected.put("objectArray", objectArray);
        JSONArray mixedArray = new JSONArray();
        mixedArray.put("One");
        mixedArray.put(2);
        mixedArray.put(new JSONArray());
        mixedArray.put(4d);
        JSONObject objectInArray = new JSONObject();
        objectInArray.put("secondArray", new JSONArray());
        mixedArray.put(objectInArray);
        expected.put("mixedArray", mixedArray);
        JSONObject jsonObject = JsonReaderUtil.JSON_OBJECT_FACTORY.newInstance(new JsonReader(new StringReader(expected.toString())));
        Assert.assertEquals("{\"number\":5,\"objectArray\":[{\"index\":0},{\"index\":1},{\"index\":2},{\"index\":3},{\"index\":4}],\"boolean\":true,\"string\":\"This is a string\",\"mixedArray\":[\"One\",2,[],4,{\"secondArray\":[]}],\"intArray\":[1,2,3]}", jsonObject.toString());
    }

    private static class ConformingType {
        private String name = null;

        public ConformingType(JsonReader jsonReader) throws IOException {
            jsonReader.beginObject();
            while (jsonReader.hasNext()) {
                switch (jsonReader.nextName()) {
                    case "name":
                        this.name = jsonReader.nextString();
                        break;
                    default:
                        jsonReader.skipValue();
                }
            }
            jsonReader.endObject();
        }
    }
}
