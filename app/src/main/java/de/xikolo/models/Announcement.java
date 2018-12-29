package de.xikolo.models;

import com.squareup.moshi.Json;

import java.util.Date;

import de.xikolo.models.base.RealmAdapter;
import de.xikolo.utils.DateUtil;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.HasOne;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class Announcement extends RealmObject {

    public static Announcement get(String id) {
        Realm realm = Realm.getDefaultInstance();
        Announcement model = realm
            .where(Announcement.class)
            .equalTo("id", id)
            .findFirst();
        if (model != null) {
            model = realm.copyFromRealm(model);
        }
        return model;
    }

    public static Long countNotVisited() {
        Realm realm = Realm.getDefaultInstance();
        return realm
            .where(Announcement.class)
            .equalTo("visited", false)
            .count();
    }

    @PrimaryKey
    public String id;

    public String title;

    public String text;

    public String imageUrl;

    public Date publishedAt;

    public boolean visited;

    public String courseId;

    @JsonApi(type = "announcements")
    public static class JsonModel extends Resource implements RealmAdapter<Announcement> {

        public String title;

        public String text;

        @Json(name = "image_url")
        public String imageUrl;

        @Json(name = "published_at")
        public String publishedAt;

        public boolean visited;

        public HasOne<Course.JsonModel> course;

        @Override
        public Announcement convertToRealmObject() {
            Announcement model = new Announcement();
            model.id = getId();
            model.title = title;
            model.text = text;
            model.imageUrl = imageUrl;
            model.publishedAt = DateUtil.parse(publishedAt);
            model.visited = visited;

            if (course != null) {
                model.courseId = course.get().getId();
            }

            return model;
        }

    }

}
