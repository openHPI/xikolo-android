package de.xikolo.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;


public class Enrollment extends RealmObject implements JsonAdapter<Enrollment.JsonModel> {

    @PrimaryKey
    public String id;

    public boolean completed;

    public boolean reactivated;

    @Override
    public JsonModel convertToJsonResource() {
        JsonModel model = new JsonModel();
        model.setId(id);
        model.completed = completed;
        model.reactivated = reactivated;
        return model;
    }

    @JsonApi(type = "enrollments")
    public static class JsonModel extends Resource implements RealmAdapter<Enrollment> {

        public boolean completed;

        public boolean reactivated;

        @Override
        public Enrollment convertToRealmObject() {
            Enrollment model = new Enrollment();
            model.completed = completed;
            model.reactivated = reactivated;
            return model;
        }

    }

}
