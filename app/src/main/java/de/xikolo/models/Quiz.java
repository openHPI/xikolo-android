package de.xikolo.models;

import com.squareup.moshi.Json;

import de.xikolo.models.base.RealmAdapter;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.HasMany;
import moe.banana.jsonapi2.HasOne;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class Quiz extends RealmObject {

    @PrimaryKey
    public String id;

    public String instructions;

    public int timeLimit;

    public int allowedAttempts;

    public boolean showWelcomePage;

    public String newestSubmissionId;

    @JsonApi(type = "quizzes")
    public static class JsonModel extends Resource implements RealmAdapter<Quiz> {

        public String instructions;

        @Json(name = "time_limit")
        public int timeLimit;

        @Json(name = "allowed_attempts")
        public int allowedAttempts;

        @Json(name = "show_welcome_page")
        public boolean showWelcomePage;

        @Json(name = "questions")
        public HasMany<QuizQuestion.JsonModel> questions;

        @Json(name = "newest_user_submission")
        public HasOne<QuizSubmission.JsonModel> newestSubmission;

        @Override
        public Quiz convertToRealmObject() {
            Quiz quiz = new Quiz();
            quiz.id = getId();
            quiz.instructions = instructions;
            quiz.timeLimit = timeLimit;
            quiz.allowedAttempts = allowedAttempts;
            quiz.showWelcomePage = showWelcomePage;

            if (newestSubmission != null) {
                quiz.newestSubmissionId = newestSubmission.get().getId();
            }

            return quiz;
        }

    }

}
