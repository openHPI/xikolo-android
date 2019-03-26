package de.xikolo.models;

import de.xikolo.models.base.JsonAdapter;
import de.xikolo.models.base.RealmAdapter;
import de.xikolo.models.dao.CourseDao;
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

    public EnrollmentCertificates certificates;

    public String courseId;

    public Course getCourse() {
        return CourseDao.Unmanaged.find(courseId);
    }

    public boolean anyCertificateAchieved() {
        return (certificates != null
            && (certificates.confirmationOfParticipationUrl != null
            || certificates.recordOfAchievementUrl != null
            || certificates.qualifiedCertificateUrl != null));
    }

    @Override
    public JsonModel convertToJsonResource() {
        JsonModel model = new JsonModel();
        model.setId(id);
        model.completed = completed;
        model.proctored = proctored;
        model.reactivated = reactivated;
        model.certificates = certificates;

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

        public EnrollmentCertificates certificates;

        public HasOne<Course.JsonModel> course;

        @Override
        public Enrollment convertToRealmObject() {
            Enrollment enrollment = new Enrollment();
            enrollment.id = getId();
            enrollment.completed = completed;
            enrollment.reactivated = reactivated;
            enrollment.proctored = proctored;
            enrollment.certificates = certificates;

            if (course != null) {
                enrollment.courseId = course.get().getId();
            }

            return enrollment;
        }

    }

}
