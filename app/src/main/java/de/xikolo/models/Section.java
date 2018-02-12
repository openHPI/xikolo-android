package de.xikolo.models;

import com.squareup.moshi.Json;

import java.util.Date;
import java.util.List;

import de.xikolo.models.base.RealmAdapter;
import de.xikolo.utils.DateUtil;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.HasMany;
import moe.banana.jsonapi2.HasOne;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class Section extends RealmObject {

    @PrimaryKey
    public String id;

    public String title;

    public String description;

    public int position;

    public Date startDate;

    public Date endDate;

    public String courseId;

    public boolean accessible;

    public static Section get(String id) {
        Realm realm = Realm.getDefaultInstance();
        Section model = realm.where(Section.class).equalTo("id", id).findFirst();
        if (model != null) model = realm.copyFromRealm(model);
        realm.close();
        return model;
    }

    public static List<Section> listForCourse(String courseId) {
        Realm realm = Realm.getDefaultInstance();
        List<Section> sectionList = realm
                .where(Section.class)
                .equalTo("courseId", courseId)
                .sort("position")
                .findAll();
        if (sectionList != null) sectionList = realm.copyFromRealm(sectionList);
        realm.close();
        return sectionList;
    }

    public Course getCourse() {
        Realm realm = Realm.getDefaultInstance();
        Course model = realm.where(Course.class).equalTo("id", courseId).findFirst();
        if (model != null) model = realm.copyFromRealm(model);
        realm.close();
        return model;
    }

    public List<Item> getAccessibleItems() {
        Realm realm = Realm.getDefaultInstance();
        List<Item> items = realm.where(Item.class)
                .equalTo("sectionId", id)
                .equalTo("accessible", true)
                .sort("position")
                .findAll();
        if (items != null) items = realm.copyFromRealm(items);
        realm.close();
        return items;
    }

    public boolean hasDownloadableContent() {
        Realm realm = Realm.getDefaultInstance();
        int size = realm.where(Item.class)
                .equalTo("sectionId", id)
                .equalTo("accessible", true)
                .equalTo("contentType", Item.TYPE_VIDEO)
                .findAll()
                .size();
        realm.close();
        return size > 0;
    }

    @JsonApi(type = "course-sections")
    public static class JsonModel extends Resource implements RealmAdapter<Section> {

        public String title;

        public String description;

        public int position;

        @Json(name = "start_at")
        public String startDate;

        @Json(name = "end_at")
        public String endDate;

        @Json(name = "course")
        public HasOne<Course.JsonModel> course;

        @Json(name = "items")
        public HasMany<Item.JsonModel> items;

        public boolean accessible;

        @Override
        public Section convertToRealmObject() {
            Section section = new Section();

            section.id = getId();
            section.title = title;
            section.description = description;
            section.position = position;
            section.startDate = DateUtil.parse(startDate);
            section.endDate = DateUtil.parse(endDate);
            section.accessible = accessible;

            if (course != null) {
                section.courseId = course.get().getId();
            }

            return section;
        }

    }

}
