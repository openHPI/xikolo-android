package de.xikolo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Video implements Parcelable {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("description")
    public String description;

    @SerializedName("minutes")
    public String minutes;

    @SerializedName("seconds")
    public String seconds;

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
        parcel.writeString(description);
        parcel.writeString(minutes);
        parcel.writeString(seconds);
        parcel.writeString(url);
    }

    public Video(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        minutes = in.readString();
        seconds = in.readString();
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
        Video o = (Video) obj;
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

    public static final Creator<Video> CREATOR = new Creator<Video>() {
        public Video createFromParcel(Parcel in) {
            return new Video(in);
        }

        public Video[] newArray(int size) {
            return new Video[size];
        }
    };

}
