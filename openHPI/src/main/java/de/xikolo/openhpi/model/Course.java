package de.xikolo.openhpi.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Course implements Parcelable {

    @SerializedName("id")
    public String id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("course_code")
    public String course_code;

    @SerializedName("lecturer")
    public String lecturer;

    @SerializedName("language")
    public String language;

    @SerializedName("duration")
    public String duration;

    @SerializedName("url")
    public String url;

    @SerializedName("visual_url")
    public String visual_url;

    @SerializedName("available_from")
    public String available_from;

    @SerializedName("available_to")
    public String available_to;

    @SerializedName("locked")
    public boolean locked;

    @SerializedName("exam_available_from")
    public String exam_available_from;

    @SerializedName("exam_available_to")
    public String exam_available_to;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(course_code);
        parcel.writeString(lecturer);
        parcel.writeString(language);
        parcel.writeString(duration);
        parcel.writeString(url);
        parcel.writeString(visual_url);
        parcel.writeString(available_from);
        parcel.writeString(available_to);
        parcel.writeByte((byte) (locked ? 1 : 0 ));
        parcel.writeString(exam_available_from);
        parcel.writeString(exam_available_to);
    }

    public Course(Parcel in) {
        id = in.readString();
        name = in.readString();
        course_code = in.readString();
        lecturer = in.readString();
        language = in.readString();
        duration = in.readString();
        url = in.readString();
        visual_url = in.readString();
        available_from = in.readString();
        available_to = in.readString();
        locked = in.readByte() != 0;
        exam_available_from = in.readString();
        exam_available_to = in.readString();
    }

    public static final Parcelable.Creator<Course> CREATOR = new Parcelable.Creator<Course>() {
        public Course createFromParcel(Parcel in) {
            return new Course(in);
        }

        public Course[] newArray(int size) {
            return new Course[size];
        }
    };

}
