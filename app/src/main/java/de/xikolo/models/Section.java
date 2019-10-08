package de.xikolo.models;

import com.squareup.moshi.Json;

import java.util.Date;
import java.util.List;

import de.xikolo.models.base.RealmAdapter;
import de.xikolo.models.dao.CourseDao;
import de.xikolo.models.dao.ItemDao;
import de.xikolo.utils.extensions.DateExtensions;
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

    public Course getCourse() {
        return CourseDao.Unmanaged.find(courseId);
    }

    public boolean hasAccessibleItems() {
        return accessible && getAccessibleItems().size() > 0;
    }

    public List<Item> getAccessibleItems() {
        return ItemDao.Unmanaged.allAccessibleForSection(id);
    }

    public boolean hasDownloadableContent() {
        return ItemDao.Unmanaged.allAccessibleVideosForSection(id).size() > 0;
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
            section.startDate = DateExtensions.getAsDate(startDate);
            section.endDate = DateExtensions.getAsDate(endDate);
            section.accessible = accessible;

            if (course != null) {
                section.courseId = course.get().getId();
            }

            return section;
        }

    }

}
