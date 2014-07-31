package de.xikolo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Module implements Parcelable {

    @SerializedName("id")
    public String id;

    @SerializedName("position")
    public int position;

    @SerializedName("name")
    public String name;

    @SerializedName("type")
    public String type;

    @SerializedName("available_from")
    public String available_from;

    @SerializedName("available_to")
    public String available_to;

    @SerializedName("locked")
    public boolean locked;

    @SerializedName("progress")
    public Progress progress;

    public List<Item> items;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeInt(position);
        parcel.writeString(name);
        parcel.writeString(type);
        parcel.writeString(available_from);
        parcel.writeString(available_to);
        parcel.writeByte((byte) (locked ? 1 : 0 ));
        parcel.writeTypedList(items);
        parcel.writeParcelable(progress, i);
    }

    public Module() {
        this.items = new ArrayList<Item>();
    }

    public Module(Parcel in) {
        this();
        id = in.readString();
        position = in.readInt();
        name = in.readString();
        type = in.readString();
        available_from = in.readString();
        available_to = in.readString();
        locked = in.readByte() != 0;
        in.readTypedList(items, Item.CREATOR);
        progress = in.readParcelable(Module.class.getClassLoader());
    }

    public static final Creator<Module> CREATOR = new Creator<Module>() {
        public Module createFromParcel(Parcel in) {
            return new Module(in);
        }

        public Module[] newArray(int size) {
            return new Module[size];
        }
    };

}
