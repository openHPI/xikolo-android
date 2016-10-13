package de.xikolo.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.lang.reflect.Type;

import de.xikolo.R;
import de.xikolo.storages.databases.DatabaseModel;

public class Item<T extends ItemDetail> implements DatabaseModel, Parcelable, Serializable {

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_SELFTEST = "self test";
    public static final String TYPE_LTI = "lti_exercise";
    public static final String TYPE_PEER = "peer_assessment";

    public static final String EXERCISE_TYPE_SELFTEST = "selftest";
    public static final String EXERCISE_TYPE_SURVEY = "survey";
    public static final String EXERCISE_TYPE_ASSIGNMENT = "main";
    public static final String EXERCISE_TYPE_BONUS = "bonus";

    public static String getIcon(Context context, String itemType, String exerciseType) {
        if (context == null) {
            return null;
        }

        int icon = R.string.icon_text;

        switch (itemType) {
            case TYPE_TEXT:
                icon = R.string.icon_text;
                break;
            case TYPE_VIDEO:
                icon = R.string.icon_video;
                break;
            case TYPE_SELFTEST:
                if (exerciseType != null && !exerciseType.equals("")) {
                    switch (exerciseType) {
                        case EXERCISE_TYPE_SELFTEST:
                            icon = R.string.icon_selftest;
                            break;
                        case EXERCISE_TYPE_SURVEY:
                            icon = R.string.icon_survey;
                            break;
                        case EXERCISE_TYPE_ASSIGNMENT:
                            icon = R.string.icon_assignment;
                            break;
                        case EXERCISE_TYPE_BONUS:
                            icon = R.string.icon_bonus;
                            break;
                    }
                } else {
                    icon = R.string.icon_selftest;
                }
                break;
            case TYPE_PEER:
                icon = R.string.icon_assignment;
                break;
            case TYPE_LTI:
                icon = R.string.icon_lti;
                break;
        }

        return context.getString(icon);
    }

    public static Type getTypeToken(String itemType) {
        if (itemType == null) {
            return null;
        }

        switch (itemType) {
            case TYPE_TEXT:
                return new TypeToken<Item<TextItemDetail>>() {
                }.getType();
            case TYPE_VIDEO:
                return new TypeToken<Item<VideoItemDetail>>() {
                }.getType();
            case TYPE_SELFTEST:
                return new TypeToken<Item<AssignmentItemDetail>>() {
                }.getType();
            case TYPE_PEER:
                return new TypeToken<Item<LtiItemDetail>>() {
                }.getType();
            case TYPE_LTI:
                return new TypeToken<Item<PeerAssessmentItemDetail>>() {
                }.getType();
        }

        return null;
    }

    @SerializedName("id")
    public String id;

    @SerializedName("position")
    public int position;

    @SerializedName("title")
    public String title;

    @SerializedName("type")
    public String type;

    @SerializedName("available_from")
    public String available_from;

    @SerializedName("available_to")
    public String available_to;

    @SerializedName("locked")
    public boolean locked;

    @SerializedName("exercise_type")
    public String exercise_type;

    public String courseId;

    public String moduleId;

    @SerializedName("object")
    public T detail;

    @SerializedName("progress")
    public Progress progress;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeInt(position);
        parcel.writeString(title);
        parcel.writeString(type);
        parcel.writeString(available_from);
        parcel.writeString(available_to);
        parcel.writeByte((byte) (locked ? 1 : 0));
        parcel.writeString(exercise_type);
        parcel.writeString(courseId);
        parcel.writeString(moduleId);
        parcel.writeParcelable(detail, i);
        parcel.writeParcelable(progress, i);
    }

    public Item(Parcel in) {
        id = in.readString();
        position = in.readInt();
        title = in.readString();
        type = in.readString();
        available_from = in.readString();
        available_to = in.readString();
        locked = in.readByte() != 0;
        exercise_type = in.readString();
        courseId = in.readString();
        moduleId = in.readString();
        detail = in.readParcelable(Item.class.getClassLoader());
        progress = in.readParcelable(Item.class.getClassLoader());
    }

    public Item() {
        progress = new Progress();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (((Object) this).getClass() != obj.getClass())
            return false;
        Item o = (Item) obj;
        if (id == null) {
            if (o.id != null)
                return false;
        } else if (!id.equals(o.id))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 11;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public static class Progress implements Parcelable, Serializable {

        @SerializedName("visited")
        public boolean visited;

        @SerializedName("completed")
        public boolean completed;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeByte((byte) (visited ? 1 : 0));
            parcel.writeByte((byte) (completed ? 1 : 0));
        }

        public Progress(Parcel in) {
            visited = in.readByte() != 0;
            completed = in.readByte() != 0;
        }

        public Progress() {

        }

        public static final Creator<Progress> CREATOR = new Creator<Progress>() {
            public Progress createFromParcel(Parcel in) {
                return new Progress(in);
            }

            public Progress[] newArray(int size) {
                return new Progress[size];
            }
        };

    }

}
