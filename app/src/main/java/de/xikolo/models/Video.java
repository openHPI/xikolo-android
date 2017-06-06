package de.xikolo.models;

import com.squareup.moshi.Json;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class Video extends RealmObject {

    @PrimaryKey
    public String id;

    public String title;

    public String summary;

    public int duration;

    public String slidesUrl;

    public String transcriptUrl;

    public String thumbnailUrl;

    public String itemId;

    public VideoStream singleStream;

    @JsonApi(type = "videos")
    public static class JsonModel extends Resource implements RealmAdapter<Video> {

        public String title;

        public String summary;

        public int duration;

        @Json(name = "slides_url")
        public String slidesUrl;

        @Json(name = "transcript_url")
        public String transcriptUrl;

        @Json(name = "thumbnail_url")
        public String thumbnailUrl;

        public String itemId;

        @Json(name = "single_stream")
        public VideoStream singleStream;

        @Override
        public Video convertToRealmObject() {
            Video video = new Video();
            video.id = getId();
            video.title = title;
            video.summary = summary;
            video.duration = duration;
            video.slidesUrl = slidesUrl;
            video.transcriptUrl = transcriptUrl;
            video.thumbnailUrl = thumbnailUrl;
            video.itemId = itemId;
            video.singleStream = singleStream;

            return video;
        }

    }

}
