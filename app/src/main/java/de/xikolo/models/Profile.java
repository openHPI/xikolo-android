package de.xikolo.models;

import com.squareup.moshi.Json;

import de.xikolo.models.base.RealmAdapter;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;


public class Profile extends RealmObject {

    @PrimaryKey
    public String id;

    public String firstName;

    public String lastName;

    public String displayName;

    public String email;

    @JsonApi(type = "user-profile")
    public static class JsonModel extends Resource implements RealmAdapter<Profile> {

        @Json(name = "first_name")
        public String firstName;

        @Json(name = "last_name")
        public String lastName;

        @Json(name = "display_name")
        public String displayName;

        public String email;

        @Override
        public Profile convertToRealmObject() {
            Profile model = new Profile();
            model.id = getId();
            model.firstName = firstName;
            model.lastName = lastName;
            model.displayName = displayName;
            model.email = email;
            return model;
        }

    }

}
