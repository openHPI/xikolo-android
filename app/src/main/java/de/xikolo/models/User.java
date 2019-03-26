package de.xikolo.models;

import com.squareup.moshi.Json;

import de.xikolo.models.base.RealmAdapter;
import de.xikolo.models.dao.ProfileDao;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.HasOne;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;


public class User extends RealmObject {

    @PrimaryKey
    public String id;

    public String name;

    public String avatarUrl;

    public String profileId;

    public Profile getProfile() {
        return ProfileDao.Unmanaged.find(profileId);
    }

    @JsonApi(type = "users")
    public static class JsonModel extends Resource implements RealmAdapter<User> {

        public String name;

        @Json(name = "avatar_url")
        public String avatarUrl;

        @Json(name = "profile")
        public HasOne<Profile.JsonModel> profile;

        @Override
        public User convertToRealmObject() {
            User model = new User();
            model.id = getId();
            model.name = name;
            model.avatarUrl = avatarUrl;

            if (profile != null) {
                model.profileId = profile.get().getId();
            }

            return model;
        }

    }

}
