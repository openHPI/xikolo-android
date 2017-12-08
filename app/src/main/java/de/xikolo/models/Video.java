package de.xikolo.models;

import com.squareup.moshi.Json;

import de.xikolo.models.base.RealmAdapter;
import io.realm.Realm;
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

    public VideoStream singleStream;

    public String slidesUrl;

    public int slidesSize;

    public String audioUrl;

    public int audioSize;

    public String transcriptUrl;

    public int transcriptSize;

    public String thumbnailUrl;

    // local field
    public int progress = 0;

    public static Video get(String id) {
        Realm realm = Realm.getDefaultInstance();
        Video model = realm.where(Video.class).equalTo("id", id).findFirst();
        realm.close();
        return model;
    }

    public static Video getForContentId(String contentId) {
        Realm realm = Realm.getDefaultInstance();
        Video model = realm.where(Video.class).equalTo("id", contentId).findFirst();
        realm.close();
        return model;
    }

    @JsonApi(type = "videos")
    public static class JsonModel extends Resource implements RealmAdapter<Video> {

        public String title;

        public String summary;

        public int duration;

        @Json(name = "single_stream")
        public VideoStream singleStream;

        @Json(name = "slides_url")
        public String slidesUrl;

        @Json(name = "slides_size")
        public int slidesSize;

        @Json(name = "audio_url")
        public String audioUrl;

        @Json(name = "audio_size")
        public int audioSize;

        @Json(name = "transcript_url")
        public String transcriptUrl;

        @Json(name = "transcript_size")
        public int transcriptSize;

        @Json(name = "thumbnail_url")
        public String thumbnailUrl;

        @Override
        public Video convertToRealmObject() {
            Video video = new Video();
            video.id = getId();
            video.title = title;
            video.summary = summary;
            video.duration = duration;
            video.singleStream = singleStream;
            video.slidesUrl = slidesUrl;
            video.slidesSize = slidesSize;
            video.audioUrl = audioUrl;
            video.audioSize = audioSize;
            video.transcriptUrl = transcriptUrl;
            video.transcriptSize = transcriptSize;
            video.thumbnailUrl = thumbnailUrl;

            return video;
        }

    }

}
