package de.xikolo.models;

import java.util.Map;

import de.xikolo.models.base.JsonAdapter;
import de.xikolo.models.base.RealmAdapter;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.HasOne;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;


public class Enrollment extends RealmObject implements JsonAdapter<Enrollment.JsonModel> {

    @PrimaryKey
    public String id;

    public boolean completed;

    public boolean reactivated;

    public boolean proctored;

    public String confirmationOfParticipationUrl;

    public String recordOfAchievementUrl;

    public String qualifiedCertificateUrl;

    public String courseId;

    public static Enrollment get(String id) {
        Realm realm = Realm.getDefaultInstance();
        Enrollment model = realm.where(Enrollment.class).equalTo("id", id).findFirst();
        if (model != null) model = realm.copyFromRealm(model);
        realm.close();
        return model;
    }

    public static Enrollment getForCourse(String courseId) {
        Realm realm = Realm.getDefaultInstance();
        Enrollment model = realm.where(Enrollment.class).equalTo("courseId", courseId).findFirst();
        if (model != null) model = realm.copyFromRealm(model);
        realm.close();
        return model;
    }

    @Override
    public JsonModel convertToJsonResource() {
        JsonModel model = new JsonModel();
        model.setId(id);
        model.completed = completed;
        model.proctored = proctored;
        model.reactivated = reactivated;

        if (courseId != null) {
            model.course = new HasOne<>(new Course.JsonModel().getType(), courseId);
        }

        return model;
    }

    @JsonApi(type = "enrollments")
    public static class JsonModel extends Resource implements RealmAdapter<Enrollment> {

        public boolean completed;

        public boolean reactivated;

        public boolean proctored;

        public Map<String, String> certificates;

        public HasOne<Course.JsonModel> course;

        @Override
        public Enrollment convertToRealmObject() {
            Enrollment enrollment = new Enrollment();
            enrollment.id = getId();
            enrollment.completed = completed;
            enrollment.reactivated = reactivated;
            enrollment.proctored = proctored;
            enrollment.confirmationOfParticipationUrl = certificates.get("confirmation_of_participation");
            enrollment.recordOfAchievementUrl = certificates.get("record_of_achievement");
            enrollment.qualifiedCertificateUrl = certificates.get("qualified_certificate");

            if (course != null) {
                enrollment.courseId = course.get().getId();
            }

            return enrollment;
        }

    }

}
