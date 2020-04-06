package de.xikolo.models;

import com.squareup.moshi.Json;

import de.xikolo.models.base.RealmAdapter;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.HasMany;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class CourseProgress extends RealmObject {

    @PrimaryKey
    public String id;

    public ExerciseStatistic mainExercises;

    public ExerciseStatistic selftestExercises;

    public ExerciseStatistic bonusExercises;

    public VisitStatistic visits;

    @JsonApi(type = "course-progresses")
    public static class JsonModel extends Resource implements RealmAdapter<CourseProgress> {

        @Json(name = "main_exercises")
        public ExerciseStatistic mainExercises;

        @Json(name = "selftest_exercises")
        public ExerciseStatistic selftestExercises;

        @Json(name = "bonus_exercises")
        public ExerciseStatistic bonusExercises;

        @Json(name = "visits")
        public VisitStatistic visits;

        @Json(name = "section_progresses")
        public HasMany<SectionProgress.JsonModel> sectionProgresses;

        @Override
        public CourseProgress convertToRealmObject() {
            CourseProgress cp = new CourseProgress();
            cp.id = getId();
            cp.mainExercises = mainExercises;
            cp.selftestExercises = selftestExercises;
            cp.bonusExercises = bonusExercises;
            cp.visits = visits;

            return cp;
        }

    }

}
