package de.xikolo.models;

import com.squareup.moshi.Json;

import java.util.List;

import de.xikolo.models.base.RealmAdapter;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.HasMany;
import moe.banana.jsonapi2.HasOne;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class SubtitleTrack extends RealmObject {

    @PrimaryKey
    public String id;

    public String language;

    public String videoId;

    public int getTextPosition(long millis) {
        Realm realm = Realm.getDefaultInstance();
        List<SubtitleCue> cueList = realm.where(SubtitleCue.class).equalTo("subtitleId", id).findAllSorted("identifier");
        realm.close();

        for (int i = 0; i < cueList.size(); i++) {
            SubtitleCue cue = cueList.get(i);
            if (cue.startAsMillis() <= millis && millis < cue.endAsMillis()) {
                return i;
            }
        }
        return -1;
    }

    @JsonApi(type = "subtitle-tracks")
    public static class JsonModel extends Resource implements RealmAdapter<SubtitleTrack> {

        @Json(name = "src_lang")
        public String language;

        public HasOne<Video.JsonModel> video;

        public HasMany<SubtitleCue.JsonModel> cues;

        @Override
        public SubtitleTrack convertToRealmObject() {
            SubtitleTrack model = new SubtitleTrack();
            model.id = getId();
            model.language = language;

            if (video != null) {
                model.videoId = video.get().getId();
            }

            return model;
        }

    }

}
