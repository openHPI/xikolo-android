package de.xikolo.models.certificates;

import android.util.Log;

import com.squareup.moshi.Json;

import java.io.Serializable;
import java.util.Map;

import de.xikolo.config.Config;
import de.xikolo.models.base.RealmAdapter;
import io.realm.RealmObject;

public class Certificates extends RealmObject {

    private static final String TAG = Certificates.class.getSimpleName();

    public ConfirmationOfParticipation confirmationOfParticipation = new ConfirmationOfParticipation();

    public RecordOfAchievement recordOfAchievement = new RecordOfAchievement();

    public QualifiedCertificate qualifiedCertificate = new QualifiedCertificate();

    public static class JsonModel implements RealmAdapter<Certificates>, Serializable {

        @Json(name = "confirmation_of_participation")
        public Map<String, Object> confirmationOfParticipation;

        @Json(name = "record_of_achievement")
        public Map<String, Object> recordOfAchievement;

        @Json(name = "qualified_certificate")
        public Map<String, Object> qualifiedCertificate;

        public Certificates convertToRealmObject() {
            Certificates c = new Certificates();

            try {
                c.confirmationOfParticipation.available = (Boolean) confirmationOfParticipation.get("available");
                c.confirmationOfParticipation.threshold = (Double) confirmationOfParticipation.get("threshold");

                c.recordOfAchievement.available = (Boolean) recordOfAchievement.get("available");
                c.recordOfAchievement.threshold = (Double) recordOfAchievement.get("threshold");

                c.qualifiedCertificate.available = (Boolean) qualifiedCertificate.get("available");
            } catch (Exception e) {
                c = null;
                if (Config.DEBUG)
                    Log.d(TAG, "Could not parse Certificates: " + e.toString());
            }

            return c;
        }
    }
}
