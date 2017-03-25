package de.xikolo.models;

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

    public String courseId;

    @Override
    public JsonModel convertToJsonResource() {
        JsonModel model = new JsonModel();
        model.setId(id);
        model.completed = completed;
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

        public HasOne<Course.JsonModel> course;

        @Override
        public Enrollment convertToRealmObject() {
            Enrollment enrollment = new Enrollment();
            enrollment.id = getId();
            enrollment.completed = completed;
            enrollment.reactivated = reactivated;

            if (course != null) {
                enrollment.courseId = course.get().getId();
            }

            return enrollment;
        }

    }

}
