package de.xikolo.models;

import com.squareup.moshi.Json;

import io.realm.RealmObject;

public class ExerciseStatistic extends RealmObject {

    @Json(name = "exercises_available")
    public int exercisesAvailable;

    @Json(name = "exercises_taken")
    public int exercisesTaken;

    @Json(name = "points_possible")
    public float pointsPossible;

    @Json(name = "points_scored")
    public float pointsScored;

}
