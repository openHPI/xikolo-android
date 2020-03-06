package de.xikolo.models;

import androidx.annotation.StringRes;

import com.squareup.moshi.Json;

import java.util.Date;

import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.models.base.RealmAdapter;
import de.xikolo.models.dao.CourseDao;
import de.xikolo.models.dao.ItemDao;
import de.xikolo.models.dao.SectionDao;
import de.xikolo.utils.extensions.DateUtil;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.HasOne;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class Item extends RealmObject {

    @PrimaryKey
    public String id;

    public String title;

    public int position;

    public Date deadline;

    public String contentType;

    public String exerciseType;

    public float maxPoints;

    public boolean proctored;

    public boolean visited;

    public boolean accessible;

    public String contentId;

    public String sectionId;

    public String courseId;

    public int timeEffort;

    public Section getSection() {
        return SectionDao.Unmanaged.find(sectionId);
    }

    public Course getCourse() {
        return CourseDao.Unmanaged.find(courseId);
    }

    public RealmObject getContent() {
        return ItemDao.Unmanaged.findContent(contentId);
    }

    public static final String TYPE_TEXT = "rich_text";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_QUIZ = "quiz";
    public static final String TYPE_LTI = "lti_exercise";
    public static final String TYPE_PEER = "peer_assessment";

    public static final String EXERCISE_TYPE_SELFTEST = "selftest";
    public static final String EXERCISE_TYPE_SURVEY = "survey";
    public static final String EXERCISE_TYPE_MAIN = "main";
    public static final String EXERCISE_TYPE_BONUS = "bonus";

    @StringRes
    public int getIconRes() {
        int icon = R.string.icon_text;

        switch (contentType) {
            case TYPE_TEXT:
                icon = R.string.icon_text;
                break;
            case TYPE_VIDEO:
                icon = R.string.icon_video;
                break;
            case TYPE_QUIZ:
                if (exerciseType != null && !exerciseType.equals("")) {
                    switch (exerciseType) {
                        case EXERCISE_TYPE_SELFTEST:
                            icon = R.string.icon_selftest;
                            break;
                        case EXERCISE_TYPE_SURVEY:
                            icon = R.string.icon_survey;
                            break;
                        case EXERCISE_TYPE_MAIN:
                            icon = R.string.icon_assignment;
                            break;
                        case EXERCISE_TYPE_BONUS:
                            icon = R.string.icon_bonus;
                            break;
                    }
                } else {
                    icon = R.string.icon_selftest;
                }
                break;
            case TYPE_PEER:
                icon = R.string.icon_assignment;
                break;
            case TYPE_LTI:
                icon = R.string.icon_lti;
                break;
        }

        return icon;
    }

    public String formatTimeEffort() {
        int hours = timeEffort / 3600;
        int minutes = (int) Math.ceil((timeEffort % 3600) / 60.0);

        if (hours > 0) {
            if (minutes > 0) {
                return String.format(App.getInstance().getString(R.string.item_duration_hour_minute), hours, minutes);
            } else {
                return String.format(App.getInstance().getString(R.string.item_duration_hour), hours);
            }
        } else if (minutes > 0) {
            return String.format(App.getInstance().getString(R.string.item_duration_minute), minutes);
        } else {
            return null;
        }


    }

    @JsonApi(type = "course-items")
    public static class JsonModel extends Resource implements RealmAdapter<Item> {

        public String title;

        public int position;

        public String deadline;

        @Json(name = "content_type")
        public String contentType;

        public HasOne<Section.JsonModel> section;

        @Json(name = "exercise_type")
        public String exerciseType;

        @Json(name = "max_points")
        public float maxPoints;

        public boolean proctored;

        public boolean visited;

        public boolean accessible;

        public HasOne<?> content;

        public HasOne<Course.JsonModel> course;

        @Json(name = "time_effort")
        public Integer timeEffort;

        @Override
        public Item convertToRealmObject() {
            Item item = new Item();

            item.id = getId();
            item.title = title;
            item.position = position;
            item.deadline = DateUtil.getAsDate(deadline);
            item.contentType = contentType;
            item.exerciseType = exerciseType;
            item.maxPoints = maxPoints;
            item.proctored = proctored;
            item.visited = visited;
            item.accessible = accessible;

            if (timeEffort != null) {
                item.timeEffort = timeEffort;
            }

            if (content != null) {
                item.contentId = content.get().getId();
            }

            if (section != null) {
                item.sectionId = section.get().getId();
            }

            if (course != null) {
                item.courseId = course.get().getId();
            }

            return item;
        }

    }

}
