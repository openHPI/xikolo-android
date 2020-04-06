package de.xikolo.models;

import com.squareup.moshi.Json;

import de.xikolo.models.base.RealmAdapter;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.HasOne;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class SectionProgress extends RealmObject {

    @PrimaryKey
    public String id;

    public String title;

    public String description;

    public int position;

    public ExerciseStatistic mainExercises;

    public ExerciseStatistic selftestExercises;

    public ExerciseStatistic bonusExercises;

    public VisitStatistic visits;

    public String courseProgressId;

    @JsonApi(type = "section-progresses")
    public static class JsonModel extends Resource implements RealmAdapter<SectionProgress> {

        public String title;

        public String description;

        public int position;

        @Json(name = "main_exercises")
        public ExerciseStatistic mainExercises;

        @Json(name = "selftest_exercises")
        public ExerciseStatistic selftestExercises;

        @Json(name = "bonus_exercises")
        public ExerciseStatistic bonusExercises;

        @Json(name = "visits")
        public VisitStatistic visits;

        @Json(name = "course_progress")
        public HasOne<CourseProgress.JsonModel> courseProgress;

        @Override
        public SectionProgress convertToRealmObject() {
            SectionProgress cp = new SectionProgress();
            cp.id = getId();
            cp.title = title;
            cp.description = description;
            cp.position = position;
            cp.mainExercises = mainExercises;
            cp.selftestExercises = selftestExercises;
            cp.bonusExercises = bonusExercises;
            cp.visits = visits;

            if (courseProgress != null) {
                cp.courseProgressId = courseProgress.get().getId();
            }

            return cp;
        }

    }

}
