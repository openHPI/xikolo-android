package de.xikolo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import de.xikolo.storages.databases.DatabaseModel;

public class VideoItemDetail extends ItemDetail implements DatabaseModel {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("minutes")
    public String minutes;

    @SerializedName("seconds")
    public String seconds;

    public int progress;

    @SerializedName("url")
    public String url;

    @SerializedName("download_url")
    public String download_url;

    @SerializedName("slides_url")
    public String slides_url;

    @SerializedName("transcript_url")
    public String transcript_url;

    @SerializedName("pip_stream")
    public Stream stream;

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
        parcel.writeString(title);
        parcel.writeString(minutes);
        parcel.writeString(seconds);
        parcel.writeInt(progress);
        parcel.writeString(url);
        parcel.writeString(download_url);
        parcel.writeString(slides_url);
        parcel.writeString(transcript_url);
        parcel.writeParcelable(stream, i);
    }

    public VideoItemDetail(Parcel in) {
        id = in.readString();
        title = in.readString();
        minutes = in.readString();
        seconds = in.readString();
        progress = in.readInt();
        url = in.readString();
        download_url = in.readString();
        slides_url = in.readString();
        transcript_url = in.readString();
        stream =  in.readParcelable(VideoItemDetail.class.getClassLoader());
    }

    public VideoItemDetail() {
        stream = new Stream();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (((Object) this).getClass() != obj.getClass())
            return false;
        VideoItemDetail o = (VideoItemDetail) obj;
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

    public static final Creator<VideoItemDetail> CREATOR = new Creator<VideoItemDetail>() {
        public VideoItemDetail createFromParcel(Parcel in) {
            return new VideoItemDetail(in);
        }

        public VideoItemDetail[] newArray(int size) {
            return new VideoItemDetail[size];
        }
    };

    public static class Stream implements Parcelable, Serializable {

        @SerializedName("hd_url")
        public String hd_url;

        @SerializedName("sd_url")
        public String sd_url;

        @SerializedName("vimeo_id")
        public String vimeo_id;

        @SerializedName("poster")
        public String poster;

        public Stream(Parcel in) {
            hd_url = in.readString();
            sd_url = in.readString();
            vimeo_id = in.readString();
            poster = in.readString();
        }

        public Stream() {

        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(hd_url);
            parcel.writeString(sd_url);
            parcel.writeString(vimeo_id);
            parcel.writeString(poster);
        }

        public static final Creator<Stream> CREATOR = new Creator<Stream>() {
            public Stream createFromParcel(Parcel in) {
                return new Stream(in);
            }

            public Stream[] newArray(int size) {
                return new Stream[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

    }

}
