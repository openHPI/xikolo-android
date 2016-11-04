package de.xikolo.models;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

public class LtiItemDetail extends ItemDetail {

    @SerializedName("id")
    public String id;

    @SerializedName("url")
    public String url;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(url);
    }

    public LtiItemDetail(Parcel in) {
        id = in.readString();
        url = in.readString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (((Object) this).getClass() != obj.getClass())
            return false;
        LtiItemDetail o = (LtiItemDetail) obj;
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

    public static final Creator<LtiItemDetail> CREATOR = new Creator<LtiItemDetail>() {
        public LtiItemDetail createFromParcel(Parcel in) {
            return new LtiItemDetail(in);
        }

        public LtiItemDetail[] newArray(int size) {
            return new LtiItemDetail[size];
        }
    };

}
