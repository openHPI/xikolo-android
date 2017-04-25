package de.xikolo.models;

import android.support.annotation.StringRes;

import com.squareup.moshi.Json;

import java.util.Date;

import de.xikolo.R;
import de.xikolo.utils.DateUtil;
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

    public String type;

    public String exerciseType;

    public boolean proctored;

    public boolean visited;

    public String sectionId;

    public static final String TYPE_TEXT = "rich_text";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_QUIZ = "quiz";
    public static final String TYPE_LTI = "lti_exercise";
    public static final String TYPE_PEER = "peer_assessment";

    public static final String EXERCISE_TYPE_SELFTEST = "selftest";
    public static final String EXERCISE_TYPE_SURVEY = "survey";
    public static final String EXERCISE_TYPE_MAIN = "main";
    public static final String EXERCISE_TYPE_BONUS = "bonus";

    public @StringRes int getIconRes() {
        int icon = R.string.icon_text;

        switch (type) {
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

        public String type;

        public HasOne<Section.JsonModel> section;

        @Json(name = "exercise_type")
        public String exerciseType;

        public boolean proctored;

        public boolean visited;

        public HasOne content;

        @Override
        public Item convertToRealmObject() {
            Item item = new Item();

            item.id = getId();
            item.title = title;
            item.position = position;
            item.deadline = DateUtil.parse(deadline);
            item.type = type;
            item.exerciseType = exerciseType;
            item.proctored = proctored;
            item.visited = visited;

            if (section != null) {
                item.sectionId = section.get().getId();
            }

            return item;
        }

    }

}
