package de.xikolo.models.certificates;

import com.squareup.moshi.Json;

import io.realm.RealmObject;

public class CertificateUrls extends RealmObject {

    @Json(name = "confirmation_of_participation")
    public String confirmationOfParticipation;

    @Json(name = "record_of_achievement")
    public String recordOfAchievement;

    @Json(name = "qualified_certificate")
    public String qualifiedCertificate;
}
