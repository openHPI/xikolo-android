package de.xikolo.models;

import android.os.Parcel;

public class PeerAssessmentItemDetail extends ItemDetail {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }

    public static final Creator<PeerAssessmentItemDetail> CREATOR = new Creator<PeerAssessmentItemDetail>() {
        public PeerAssessmentItemDetail createFromParcel(Parcel in) {
            return new PeerAssessmentItemDetail();
        }

        public PeerAssessmentItemDetail[] newArray(int size) {
            return new PeerAssessmentItemDetail[size];
        }
    };

}
