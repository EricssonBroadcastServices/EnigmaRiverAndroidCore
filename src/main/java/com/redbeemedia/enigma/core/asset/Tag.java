package com.redbeemedia.enigma.core.asset;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.SortedSet;
import java.util.TreeSet;

public class Tag implements Parcelable {

    private static final int NULL_TREE_VALUE = -1;

    private String type;
    private SortedSet<Value> tagValues;

    public Tag() {}

    public String getType() {
        return type;
    }

    public SortedSet<Value> getTagValues() {
        return tagValues;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setTagValues(final SortedSet<Value> tagValues) {
        this.tagValues = tagValues;
    }

    public boolean hasTag(final Tag.Value value) {
        return tagValues != null && tagValues.contains(value);
    }

    // <editor-fold description="PARCELABLE IMPLEMENTATION">
    protected Tag(final Parcel in) {
        type = in.readString();

        final int numberOfTags = in.readInt();
        if (numberOfTags != NULL_TREE_VALUE) {
            tagValues = new TreeSet<>();
            for (int i = 0; i < numberOfTags; i++) {
                final Value value = in.readParcelable(Value.class.getClassLoader());
                tagValues.add(value);
            }
        }
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(type);

        final int numberOfTags;
        if (tagValues != null) {
            numberOfTags = tagValues.size();
        } else {
            numberOfTags = NULL_TREE_VALUE;
        }

        dest.writeInt(numberOfTags);
        if (numberOfTags != NULL_TREE_VALUE) {
            for (final Value value : tagValues) {
                dest.writeParcelable(value, flags);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Tag> CREATOR = new Creator<Tag>() {
        @Override
        public Tag createFromParcel(final Parcel in) {
            return new Tag(in);
        }

        @Override
        public Tag[] newArray(final int size) {
            return new Tag[size];
        }
    };
    // </editor-fold>

    public static class Value implements Parcelable, Comparable<Value> {

        private String tagId;

        public Value() {}

        public String getTagId() {
            return tagId;
        }

        public void setTagId(final String tagId) {
            this.tagId = tagId;
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean isEquals;
            if (Value.class.isInstance(obj)) {
                final Value otherValue = (Value) obj;

                isEquals = TextUtils.equals(tagId, otherValue.tagId);
            } else {
                isEquals = false;
            }

            return  isEquals;
        }

        // <editor-fold description="PARCEL IMPLEMENTATION">
        protected Value(final Parcel in) {
            tagId = in.readString();
        }

        @Override
        public void writeToParcel(final Parcel dest, final int flags) {
            dest.writeString(tagId);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Value> CREATOR = new Creator<Value>() {
            @Override
            public Value createFromParcel(final Parcel in) {
                return new Value(in);
            }

            @Override
            public Value[] newArray(final int size) {
                return new Value[size];
            }
        };

        @Override
        public int compareTo(final Value o) {
            final int compared;
            if (tagId == null) {
                compared = -1;
            } else if (o.tagId == null) {
                compared = 1;
            } else {
                compared = tagId.compareTo(o.tagId);
            }

            return compared;
        }
        // </editor-fold>
    }
}
