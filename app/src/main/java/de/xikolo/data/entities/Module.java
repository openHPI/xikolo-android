package de.xikolo.data.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Module implements Parcelable, Serializable {

    @SerializedName("id")
    public String id;

    @SerializedName("position")
    public int position;

    @SerializedName("name")
    public String name;

    @SerializedName("available_from")
    public String available_from;

    @SerializedName("available_to")
    public String available_to;

    @SerializedName("locked")
    public boolean locked;

    @SerializedName("progress")
    public OverallProgress progress;

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
        parcel.writeString(available_from);
        parcel.writeString(available_to);
        parcel.writeByte((byte) (locked ? 1 : 0 ));
        parcel.writeTypedList(items);
        parcel.writeParcelable(progress, i);
    }

    public Module() {
        items = new ArrayList<Item>();
        progress = new OverallProgress();
    }

    public Module(Parcel in) {
        this();
        id = in.readString();
        position = in.readInt();
        name = in.readString();
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (((Object) this).getClass() != obj.getClass())
            return false;
        Module o = (Module) obj;
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

}
