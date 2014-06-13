package de.xikolo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Item implements Parcelable {

    public static final String TYPE_TEXT = "text";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_SELFTEST = "self test";
    public static final String TYPE_ASSIGNMENT = "assignment";
    public static final String TYPE_EXAM = "exam";

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
        parcel.writeByte((byte) (locked ? 1 : 0 ));
    }

    public Item(Parcel in) {
        id = in.readString();
        position = in.readInt();
        title = in.readString();
        type = in.readString();
        available_from = in.readString();
        available_to = in.readString();
        locked = in.readByte() != 0;
    }

    public static final Creator<Item> CREATOR = new Creator<Item>() {
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

}
