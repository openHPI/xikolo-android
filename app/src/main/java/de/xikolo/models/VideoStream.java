package de.xikolo.models;

import com.squareup.moshi.Json;

import io.realm.RealmObject;

public class VideoStream extends RealmObject {

    @Json(name = "hd_url")
    public String hdUrl;

    @Json(name = "sd_url")
    public String sdUrl;

    @Json(name = "hls_url")
    public String hlsUrl;

    @Json(name = "hd_size")
    public int hdSize;

    @Json(name = "sd_size")
    public int sdSize;

    @Json(name = "thumbnail_url")
    public String thumbnailUrl;

}
