package de.xikolo.models;

import com.squareup.moshi.Json;

import io.realm.RealmObject;

public class VisitStatistic extends RealmObject {

    @Json(name = "items_available")
    public int itemsAvailable;

    @Json(name = "items_visited")
    public int itemsVisited;

}
