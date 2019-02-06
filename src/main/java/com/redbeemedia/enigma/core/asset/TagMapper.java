package com.redbeemedia.enigma.core.asset;

import android.util.JsonReader;

import java.io.IOException;
import java.util.TreeSet;

@Deprecated
public class TagMapper implements JsonMapper<Tag> {

    @Override
    public Tag create() {
        return new Tag();
    }

    @Override
    public void map(final JsonReader jsonReader,
                    final Tag tag,
                    final int position)
            throws IOException
    {
        switch (jsonReader.nextName()) {
            case "type":
                tag.setType(jsonReader.nextString());
                break;

            case "tagValues":
                final TreeSet<Tag.Value> values = new TreeSet<>();
                jsonReader.beginArray();
                while (jsonReader.hasNext()) {
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        if (jsonReader.nextName().equals("tagId")) {
                            final Tag.Value value = new Tag.Value();
                            value.setTagId(jsonReader.nextString());
                            values.add(value);
                        } else {
                            jsonReader.skipValue();
                        }
                    }
                    jsonReader.endObject();
                }
                jsonReader.endArray();
                tag.setTagValues(values);
                break;

            default:
                jsonReader.skipValue();
        }
    }

    @Override
    public Tag onMapComplete(final Tag tag, int position) {
        return tag;
    }
}

