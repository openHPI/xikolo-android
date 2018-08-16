package de.xikolo.models.certificates;

import com.squareup.moshi.Json;

import io.realm.RealmObject;

public class Certificates extends RealmObject {

    @Json(name = "confirmation_of_participation")
    public ConfirmationOfParticipation confirmationOfParticipation;

    @Json(name = "record_of_achievement")
    public RecordOfAchievement recordOfAchievement;

    @Json(name = "qualified_certificate")
    public QualifiedCertificate qualifiedCertificate;
}
