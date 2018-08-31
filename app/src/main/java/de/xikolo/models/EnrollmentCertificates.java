package de.xikolo.models;

import com.squareup.moshi.Json;

import io.realm.RealmObject;

public class EnrollmentCertificates extends RealmObject {

    @Json(name = "confirmation_of_participation")
    public String confirmationOfParticipationUrl;

    @Json(name = "record_of_achievement")
    public String recordOfAchievementUrl;

    @Json(name = "qualified_certificate")
    public String qualifiedCertificateUrl;
}
