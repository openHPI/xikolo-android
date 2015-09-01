package de.xikolo.data.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Enrollment implements Parcelable, Serializable {

    @SerializedName("id")
    public String id;

    @SerializedName("course_id")
    public String course_id;

    public Enrollment(Parcel in) {
        id = in.readString();
        course_id = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Enrollment> CREATOR = new Creator<Enrollment>() {
        public Enrollment createFromParcel(Parcel in) {
            return new Enrollment(in);
        }

        public Enrollment[] newArray(int size) {
            return new Enrollment[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(course_id);
    }

}
