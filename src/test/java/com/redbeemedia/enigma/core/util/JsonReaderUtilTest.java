package com.redbeemedia.enigma.core.util;

import android.util.JsonReader;

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
