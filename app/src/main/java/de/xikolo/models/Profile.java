package de.xikolo.models;

import com.squareup.moshi.Json;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;


public class Profile extends RealmObject {

    @PrimaryKey
    public String id;

    public String firstName;

    public String lastName;

    public String email;

    public String visualUrl;

    @JsonApi(type = "profiles")
    public static class JsonModel extends Resource implements RealmAdapter<Profile> {

        @Json(name = "first_name")
        public String firstName;

        @Json(name = "last_name")
        public String lastName;

        public String email;

        @Json(name = "visual_url")
        public String visualUrl;

        @Override
        public Profile convertToRealmObject() {
            Profile model = new Profile();
            model.firstName = firstName;
            model.lastName = lastName;
            model.email = email;
            model.visualUrl = visualUrl;

            return model;
        }

    }

}