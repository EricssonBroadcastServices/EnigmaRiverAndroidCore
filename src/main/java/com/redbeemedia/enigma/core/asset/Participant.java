package com.redbeemedia.enigma.core.asset;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.List;


public class Participant implements Parcelable {

    public static final int INDEX_ACTOR = 0;
    public static final int INDEX_DIRECTOR = 1;

    public static final int CAST_SIZE = 2;

    private static final String FUNCTION_ACTOR = "Actor";
    private static final String FUNCTION_DIRECTOR = "Director";

    private String personId;
    private String name;
    private String function;

    public Participant() {}

    public String getPersonId() {
        return personId;
    }

    public String getName() {
        return name;
    }

    public String getFunction() {
        return function;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFunction(String function) {
        this.function = function;
    }


    public static class Cast implements CharSequence {

        private final int mNumberOfCasts;
        private final String mCastString;

        public static Cast[] from(final Asset asset) {
            final Cast[] casts = new Cast[CAST_SIZE];
            final List<Participant> participants = asset.getParticipants();

            if (participants != null && !participants.isEmpty()) {
                final StringBuilder actorsBuilder = new StringBuilder();
                final StringBuilder directorsBuilder = new StringBuilder();

                int numberOfActors = 0;
                int numberOfDirectors = 0;
                for (final Participant participant : participants) {
                    final String function = participant.function;

                    if (FUNCTION_ACTOR.equalsIgnoreCase(function)) {
                        actorsBuilder.append(participant.name).append(',').append(' ');
                        numberOfActors = numberOfActors + 1;

                    } else if (FUNCTION_DIRECTOR.equalsIgnoreCase(function)) {
                        directorsBuilder.append(participant.name).append(',').append(' ');
                        numberOfDirectors = numberOfDirectors + 1;
                    }
                }

                final String actorString = castFrom(actorsBuilder);
                final String directorString = castFrom(directorsBuilder);

                casts[INDEX_ACTOR] = new Cast(numberOfActors, actorString);
                casts[INDEX_DIRECTOR] = new Cast(numberOfDirectors, directorString);
            } else {
                casts[INDEX_ACTOR] = null;
                casts[INDEX_DIRECTOR] = null;
            }

            return casts;
        }

        private static String castFrom(final StringBuilder stringBuilder) {
            final int builderLength = stringBuilder.length();

            final String casts;
            if (builderLength > 0) {
                casts = stringBuilder.substring(0, builderLength - 2);
            } else {
                casts = "";
            }

            return casts;
        }

        private Cast(final int numberOfCasts,
                     final String castString)
        {
            this.mNumberOfCasts = numberOfCasts;
            this.mCastString = castString;
        }

        public int getNumberOfCasts() {
            return mNumberOfCasts;
        }

        @Override
        public int length() {
            return mCastString.length();
        }

        @Override
        public char charAt(final int index) {
            return mCastString.charAt(index);
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            return mCastString.subSequence(start, end);
        }

        @Override
        public String toString() {
            return mCastString;
        }
    }

    // <editor-fold description="PARCEL IMPLEMENTATION">
    protected Participant(final Parcel in) {
        personId = in.readString();
        name = in.readString();
        function = in.readString();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(personId);
        dest.writeString(name);
        dest.writeString(function);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Participant> CREATOR = new Creator<Participant>() {
        @Override
        public Participant createFromParcel(final Parcel in) {
            return new Participant(in);
        }

        @Override
        public Participant[] newArray(final int size) {
            return new Participant[size];
        }
    };
    // </editor-fold>
}
