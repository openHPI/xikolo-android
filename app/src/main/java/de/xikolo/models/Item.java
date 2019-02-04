package de.xikolo.models;

import com.squareup.moshi.Json;

import java.util.Date;

import androidx.annotation.StringRes;
import de.xikolo.R;
import de.xikolo.models.base.RealmAdapter;
import de.xikolo.utils.DateUtil;
import io.realm.Realm;
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

    public boolean proctored;

    public boolean visited;

    public boolean accessible;

    public String contentId;

    public String sectionId;

    public String courseId;

    public static Item get(String id) {
        Realm realm = Realm.getDefaultInstance();
        Item model = realm.where(Item.class).equalTo("id", id).findFirst();
        if (model != null) model = realm.copyFromRealm(model);
        realm.close();
        return model;
    }

    public Section getSection() {
        Realm realm = Realm.getDefaultInstance();
        Section model = realm.where(Section.class).equalTo("id", sectionId).findFirst();
        if (model != null) model = realm.copyFromRealm(model);
        realm.close();
        return model;
    }

    public RealmObject getContent() {
        Realm realm = Realm.getDefaultInstance();

        RealmObject model = null;
        switch (contentType) {
            case TYPE_TEXT:
                model = realm.where(RichText.class).equalTo("id", contentId).findFirst();
                break;
            case TYPE_VIDEO:
                model = realm.where(Video.class).equalTo("id", contentId).findFirst();
                break;
            case TYPE_QUIZ:
                model = realm.where(Quiz.class).equalTo("id", contentId).findFirst();
                break;
            case TYPE_LTI:
                model = realm.where(LtiExercise.class).equalTo("id", contentId).findFirst();
                break;
            case TYPE_PEER:
                model = realm.where(PeerAssessment.class).equalTo("id", contentId).findFirst();
                break;
        }

        if (model != null) model = realm.copyFromRealm(model);
        realm.close();
        return model;
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

        public boolean proctored;

        public boolean visited;

        public boolean accessible;

        public HasOne<?> content;

        public HasOne<Course.JsonModel> course;

        @Override
        public Item convertToRealmObject() {
            Item item = new Item();

            item.id = getId();
            item.title = title;
            item.position = position;
            item.deadline = DateUtil.parse(deadline);
            item.contentType = contentType;
            item.exerciseType = exerciseType;
            item.proctored = proctored;
            item.visited = visited;
            item.accessible = accessible;

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
