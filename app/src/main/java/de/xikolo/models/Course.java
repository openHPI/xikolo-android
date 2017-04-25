package de.xikolo.models;

import com.squareup.moshi.Json;

import java.util.Date;

import de.xikolo.utils.DateUtil;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.HasOne;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class Course extends RealmObject implements JsonAdapter<Course.JsonModel> {

    public enum Filter {
        ALL, MY
    }

    public enum Tab {
        RESUME, PINBOARD, PROGRESS, LEARNING_ROOMS, ANNOUNCEMENTS, DETAILS
    }

    @PrimaryKey
    public String id;

    public String title;

    public String slug;

    public Date startDate;

    public Date endDate;

    public String shortAbstract;

    public String description;

    public String imageUrl;

    public String language;

    public String status;

    public String classifiers;

    public String teachers;

    public boolean accessible;

    public boolean enrollable;

    public boolean hidden;

    public boolean external;

    public String externalUrl;

    public String policyUrl;

    public boolean onDemand;

    public String enrollmentId;

    public boolean isEnrolled() {
        return enrollmentId != null;
    }

    @Override
    public JsonModel convertToJsonResource() {
        Course.JsonModel model = new Course.JsonModel();
        model.setId(id);
        model.title = title;
        model.slug = slug;
        model.startDate = DateUtil.format(startDate);
        model.endDate = DateUtil.format(endDate);
        model.shortAbstract = shortAbstract;
        model.description = description;
        model.imageUrl = imageUrl;
        model.language = language;
        model.status = status;
        model.classifiers = classifiers;
        model.teachers = teachers;
        model.accessible = accessible;
        model.enrollable = enrollable;
        model.hidden = hidden;
        model.external = external;
        model.externalUrl = externalUrl;
        model.policyUrl = policyUrl;
        model.onDemand = onDemand;

        if (enrollmentId != null) {
            model.enrollment = new HasOne<>(new Enrollment.JsonModel().getType(), enrollmentId);
        }

        return model;
    }

    @JsonApi(type = "courses")
    public static class JsonModel extends Resource implements RealmAdapter<Course> {

        public String title;

        public String slug;

        @Json(name = "start_at")
        public String startDate;

        @Json(name = "end_at")
        public String endDate;

        @Json(name = "abstract")
        public String shortAbstract;

        public String description;

        @Json(name = "image_url")
        public String imageUrl;

        public String language;

        public String status;

        public transient String classifiers;

        public String teachers;

        public boolean accessible;

        public boolean enrollable;

        public boolean hidden;

        public boolean external;

        @Json(name = "external_url")
        public String externalUrl;

        @Json(name = "policy_url")
        public String policyUrl;

        @Json(name = "qualified_certificate_available")
        public boolean qualifiedCertificateAvailable;

        @Json(name = "on_demand")
        public boolean onDemand;

        @Json(name = "user_enrollment")
        public HasOne<Enrollment.JsonModel> enrollment;

        @Override
        public Course convertToRealmObject() {
            Course course = new Course();
            course.id = getId();
            course.title = title;
            course.slug = slug;
            course.startDate = DateUtil.parse(startDate);
            course.endDate = DateUtil.parse(endDate);
            course.shortAbstract = shortAbstract;
            course.description = description;
            course.imageUrl = imageUrl;
            course.language = language;
            course.status = status;
            course.classifiers = classifiers;
            course.teachers = teachers;
            course.accessible = accessible;
            course.enrollable = enrollable;
            course.hidden = hidden;
            course.external = external;
            course.externalUrl = externalUrl;
            course.policyUrl = policyUrl;
            course.onDemand = onDemand;

            if (enrollment != null) {
                course.enrollmentId = enrollment.get().getId();
            }

            return course;
        }

    }

}
