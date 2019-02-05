package com.redbeemedia.enigma.core.asset;

import android.util.JsonReader;

import java.io.IOException;

@Deprecated
public class ParticipantMapper implements JsonMapper<Participant> {

    @Override
    public Participant create() {
        return new Participant();
    }

    @Override
    public void map(final JsonReader jsonReader, final Participant participant, final int position) throws IOException {
        switch (jsonReader.nextName()) {
            case "function":
                participant.setFunction(jsonReader.nextString());
                break;

            case "name":
                participant.setName(jsonReader.nextString());
                break;

            case "personId":
                participant.setPersonId(jsonReader.nextString());
                break;

            default:
                jsonReader.skipValue();
        }
    }

    @Override
    public Participant onMapComplete(final Participant participant,
                                     final int position)
    {
        return participant;
    }
}
