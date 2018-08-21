package de.xikolo.models;

import com.squareup.moshi.Json;

import io.realm.RealmObject;

public class CourseCertificates extends RealmObject {

    @Json(name = "confirmation_of_participation")
    public CourseCertificateDetails confirmationOfParticipation;

    @Json(name = "record_of_achievement")
    public CourseCertificateDetails recordOfAchievement;

    @Json(name = "qualified_certificate")
    public CourseCertificateDetails qualifiedCertificate;
}
