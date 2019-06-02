package de.xikolo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.moshi.Json;

import io.realm.RealmObject;

public class VideoStream extends RealmObject implements Parcelable {

    @Json(name = "hd_url")
    public String hdUrl;

    @Json(name = "sd_url")
    public String sdUrl;

    @Json(name = "hls_url")
    public String hlsUrl;

    @Json(name = "hd_size")
    public int hdSize;

    @Json(name = "sd_size")
    public int sdSize;

    @Json(name = "thumbnail_url")
    public String thumbnailUrl;

    public VideoStream(Parcel in) {
        hdUrl = in.readString();
        sdUrl = in.readString();
        hlsUrl = in.readString();
        hdSize = in.readInt();
        sdSize = in.readInt();
        thumbnailUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hdUrl);
        dest.writeString(sdUrl);
        dest.writeString(hlsUrl);
        dest.writeInt(hdSize);
        dest.writeInt(sdSize);
        dest.writeString(thumbnailUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<VideoStream> CREATOR = new Parcelable.Creator<VideoStream>() {
        @Override
        public VideoStream createFromParcel(Parcel in) {
            return new VideoStream(in);
        }

        @Override
        public VideoStream[] newArray(int size) {
            return new VideoStream[size];
        }
    };

    public VideoStream(String hdUrl, String sdUrl, String hlsUrl, int hdSize, int sdSize, String thumbnailUrl) {
        this.hdUrl = hdUrl;
        this.sdUrl = sdUrl;
        this.hlsUrl = hlsUrl;
        this.hdSize = hdSize;
        this.sdSize = sdSize;
        this.thumbnailUrl = thumbnailUrl;
    }

    @SuppressWarnings("unused")
    public VideoStream() {
    }

}
