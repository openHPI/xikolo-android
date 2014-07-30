package de.xikolo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ItemAssignment implements Parcelable {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("question_count")
    public int question_count;

    @SerializedName("points_possible")
    public int points_possible;

    @SerializedName("url")
    public String url;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeInt(question_count);
        parcel.writeInt(points_possible);
        parcel.writeString(url);
    }

    public ItemAssignment(Parcel in) {
        id = in.readString();
        title = in.readString();
        question_count = in.readInt();
        points_possible = in.readInt();
        url = in.readString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ItemAssignment o = (ItemAssignment) obj;
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

    public static final Creator<ItemAssignment> CREATOR = new Creator<ItemAssignment>() {
        public ItemAssignment createFromParcel(Parcel in) {
            return new ItemAssignment(in);
        }

        public ItemAssignment[] newArray(int size) {
            return new ItemAssignment[size];
        }
    };

}
