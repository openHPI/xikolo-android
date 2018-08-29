package de.xikolo.models;

import com.squareup.moshi.Json;

import java.util.List;

import de.xikolo.models.base.RealmAdapter;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.HasOne;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class SubtitleTrack extends RealmObject {

    @PrimaryKey
    public String id;

    public String language;

    public boolean createdByMachine;

    public String vttUrl;

    public String videoId;

    public int getTextPosition(long millis) {
        List<SubtitleCue> cueList = listCues();

        for (int i = 0; i < cueList.size(); i++) {
            SubtitleCue cue = cueList.get(i);
            if (cue.startAsMillis() <= millis && millis < cue.endAsMillis()) {
                return i;
            }
        }
        return -1;
    }

    public List<SubtitleCue> listCues() {
        Realm realm = Realm.getDefaultInstance();
        List<SubtitleCue> list = realm.where(SubtitleCue.class)
                .equalTo("subtitleId", id)
                .sort("identifier")
                .findAll();
        if (list != null) list = realm.copyFromRealm(list);
        realm.close();
        return list;
    }

    public static List<SubtitleTrack> listForVideoId(String videoId) {
        Realm realm = Realm.getDefaultInstance();
        List<SubtitleTrack> list = realm.where(SubtitleTrack.class)
                .equalTo("videoId", videoId)
                .findAll();
        if (list != null) list = realm.copyFromRealm(list);
        realm.close();
        return list;
    }

    @JsonApi(type = "subtitle-tracks")
    public static class JsonModel extends Resource implements RealmAdapter<SubtitleTrack> {

        public String language;

        @Json(name = "created_by_machine")
        public boolean createdByMachine;

        @Json(name = "vtt_url")
        public String vttUrl;

        public HasOne<Video.JsonModel> video;

        @Override
        public SubtitleTrack convertToRealmObject() {
            SubtitleTrack model = new SubtitleTrack();
            model.id = getId();
            model.createdByMachine = createdByMachine;
            model.vttUrl = vttUrl;
            model.language = language;

            if (video != null) {
                model.videoId = video.get().getId();
            }

            return model;
        }

    }

}
