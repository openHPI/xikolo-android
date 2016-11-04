package de.xikolo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import de.xikolo.storages.databases.DatabaseModel;

public class Progress implements DatabaseModel, Parcelable, Serializable {

    public String id;

    @SerializedName("items")
    public ItemCount items;

    @SerializedName("self_tests")
    public TestCount self_tests;

    @SerializedName("assignments")
    public TestCount assignments;

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
        parcel.writeParcelable(items, i);
        parcel.writeParcelable(self_tests, i);
        parcel.writeParcelable(assignments, i);
    }

    public Progress() {
        items = new ItemCount();
        self_tests = new TestCount();
        assignments = new TestCount();
    }
    
    public Progress(Parcel in) {
        this();
        id = in.readString();
        items = in.readParcelable(Progress.class.getClassLoader());
        self_tests = in.readParcelable(Progress.class.getClassLoader());
        assignments = in.readParcelable(Progress.class.getClassLoader());
    }

    public static final Parcelable.Creator<Progress> CREATOR = new Parcelable.Creator<Progress>() {
        public Progress createFromParcel(Parcel in) {
            return new Progress(in);
        }

        public Progress[] newArray(int size) {
            return new Progress[size];
        }
    };

    public static class ItemCount implements Parcelable, Serializable {

        @SerializedName("count_available")
        public int count_available;

        @SerializedName("count_visited")
        public int count_visited;

        @SerializedName("count_completed")
        public int count_completed;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(count_available);
            parcel.writeInt(count_visited);
            parcel.writeInt(count_completed);
        }
        
        public ItemCount() {
            
        }

        public ItemCount(Parcel in) {
            this();
            count_available = in.readInt();
            count_visited = in.readInt();
            count_completed = in.readInt();
        }

        public static final Parcelable.Creator<ItemCount> CREATOR = new Parcelable.Creator<ItemCount>() {
            public ItemCount createFromParcel(Parcel in) {
                return new ItemCount(in);
            }

            public ItemCount[] newArray(int size) {
                return new ItemCount[size];
            }
        };

    }

    public static class TestCount implements Parcelable, Serializable {

        @SerializedName("count_available")
        public float count_available;

        @SerializedName("count_taken")
        public float count_taken;

        @SerializedName("points_possible")
        public float points_possible;

        @SerializedName("points_scored")
        public float points_scored;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeFloat(count_available);
            parcel.writeFloat(count_taken);
            parcel.writeFloat(points_possible);
            parcel.writeFloat(points_scored);
        }
        
        public TestCount() {
            
        }

        public TestCount(Parcel in) {
            this();
            count_available = in.readFloat();
            count_taken = in.readFloat();
            points_possible = in.readFloat();
            points_scored = in.readFloat();
        }

        public static final Parcelable.Creator<TestCount> CREATOR = new Parcelable.Creator<TestCount>() {
            public TestCount createFromParcel(Parcel in) {
                return new TestCount(in);
            }

            public TestCount[] newArray(int size) {
                return new TestCount[size];
            }
        };

    }

}
