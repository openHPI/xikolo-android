package de.xikolo.models;

import com.squareup.moshi.Json;

import de.xikolo.models.base.RealmAdapter;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class Quiz extends RealmObject {

    @PrimaryKey
    public String id;

    public int timeLimit;

    public int allowedAttempts;

    public int maxPoints;

    public boolean showWelcomePage;

    public String itemId;

    @JsonApi(type = "quizzes")
    public static class JsonModel extends Resource implements RealmAdapter<Quiz> {

        @Json(name = "time_limit")
        public int timeLimit;

        @Json(name = "allowed_attempts")
        public int allowedAttempts;

        @Json(name = "max_points")
        public int maxPoints;

        @Json(name = "show_welcome_page")
        public boolean showWelcomePage;

        public String itemId;

        @Override
        public Quiz convertToRealmObject() {
            Quiz quiz = new Quiz();
            quiz.id = getId();
            quiz.timeLimit = timeLimit;
            quiz.allowedAttempts = allowedAttempts;
            quiz.maxPoints = maxPoints;
            quiz.showWelcomePage = showWelcomePage;
            quiz.itemId = itemId;

            return quiz;
        }

    }

}
