package de.xikolo.models;

import com.squareup.moshi.Json;

import java.util.Date;
import java.util.List;

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

    public static Section get(String id) {
        Realm realm = Realm.getDefaultInstance();
        Section model = realm.where(Section.class).equalTo("id", id).findFirst();
        realm.close();
        return model;
    }

    public Course getCourse() {
        Realm realm = Realm.getDefaultInstance();
        Course course = realm.where(Course.class).equalTo("id", courseId).findFirst();
        realm.close();
        return course;
    }

    public List<Item> getItems() {
        Realm realm = Realm.getDefaultInstance();
        List<Item> items = realm.where(Item.class).equalTo("sectionId", id).findAllSorted("position");
        realm.close();
        return items;
    }

    @JsonApi(type = "course-sections")
    public static class JsonModel extends Resource implements RealmAdapter<Section> {

        public String title;

        public String description;

        public int position;

        @Json(name = "start_date")
        public String startDate;

        @Json(name = "end_date")
        public String endDate;

        @Json(name = "course")
        public HasOne<Course.JsonModel> course;

        @Json(name = "items")
        public HasMany<Item.JsonModel> items;

        @Override
        public Section convertToRealmObject() {
            Section section = new Section();

            section.id = getId();
            section.title = title;
            section.description = description;
            section.position = position;
            section.startDate = DateUtil.parse(startDate);
            section.endDate = DateUtil.parse(endDate);

            if (course != null) {
                section.courseId = course.get().getId();
            }

            return section;
        }

    }

}
