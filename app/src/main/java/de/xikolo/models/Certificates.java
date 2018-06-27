package de.xikolo.models;

import java.util.Map;

import io.realm.RealmObject;

public class Certificates extends RealmObject {

    public boolean confirmationOfParticipationAvailable;
    public double confirmationOfParticipationThreshold;
    public boolean recordOfAchievementAvailable;
    public double recordOfAchievementThreshold;
    public boolean qualifiedCertificateAvailable;

    public String confirmationOfParticipationUrl = null;
    public String recordOfAchievementUrl = null;
    public String qualifiedCertificateUrl = null;

    public Certificates(Map<String, Map<String, Object>> certificates) throws ClassCastException, NullPointerException {
        this.confirmationOfParticipationAvailable = (Boolean) certificates.get("confirmation_of_participation").get("available");
        this.confirmationOfParticipationThreshold = (Double) certificates.get("confirmation_of_participation").get("threshold");
        this.recordOfAchievementAvailable = (Boolean) certificates.get("record_of_achievement").get("available");
        this.recordOfAchievementThreshold = (Double) certificates.get("record_of_achievement").get("threshold");
        this.qualifiedCertificateAvailable = (Boolean) certificates.get("qualified_certificate").get("available");
    }

    public void setCertificateUrls(String confirmationOfParticipationUrl, String recordOfAchievementUrl, String qualifiedCertificateUrl) {
        this.confirmationOfParticipationUrl = confirmationOfParticipationUrl;
        this.recordOfAchievementUrl = recordOfAchievementUrl;
        this.qualifiedCertificateUrl = qualifiedCertificateUrl;
    }
}