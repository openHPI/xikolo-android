package de.xikolo.models;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

public class TextItemDetail extends ItemDetail {

    @SerializedName("id")
    public String id;

    @SerializedName("title")
    public String title;

    @SerializedName("body")
    public String body;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(body);
    }

    public TextItemDetail(Parcel in) {
        id = in.readString();
        title = in.readString();
        body = in.readString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (((Object) this).getClass() != obj.getClass())
            return false;
        TextItemDetail o = (TextItemDetail) obj;
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

    public static final Creator<TextItemDetail> CREATOR = new Creator<TextItemDetail>() {
        public TextItemDetail createFromParcel(Parcel in) {
            return new TextItemDetail(in);
        }

        public TextItemDetail[] newArray(int size) {
            return new TextItemDetail[size];
        }
    };

}
