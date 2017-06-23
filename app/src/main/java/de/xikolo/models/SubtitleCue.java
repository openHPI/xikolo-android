package de.xikolo.models;

import java.util.concurrent.TimeUnit;

import de.xikolo.models.base.RealmAdapter;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.HasOne;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class SubtitleCue extends RealmObject {

    @PrimaryKey
    public String id;

    public String identifier;

    public String text;

    public String start;

    public String end;

    public String settings;

    public String subtitleId;

    public long startAsMillis() {
        return convertToMillis(start);
    }

    public long endAsMillis() {
        return convertToMillis(end);
    }

    private long convertToMillis(String time) {
        String[] timeUnits = time.split("[:\\.]");

        long millis = 0;

        millis += TimeUnit.HOURS.toMillis(Long.parseLong(timeUnits[0]));
        millis += TimeUnit.MINUTES.toMillis(Long.parseLong(timeUnits[1]));
        millis += TimeUnit.SECONDS.toMillis(Long.parseLong(timeUnits[2]));
        millis += TimeUnit.MILLISECONDS.toMillis(Long.parseLong(timeUnits[3]));

        return millis;
    }

    @JsonApi(type = "subtitle-cues")
    public static class JsonModel extends Resource implements RealmAdapter<SubtitleCue> {

        public String identifier;

        public String text;

        public String start;

        public String end;

        public String settings;

        public HasOne<SubtitleTrack.JsonModel> subtitle;

        @Override
        public SubtitleCue convertToRealmObject() {
            SubtitleCue model = new SubtitleCue();
            model.id = getId();
            model.identifier = identifier;
            model.text = text;
            model.start = start;
            model.end = end;
            model.settings = settings;

            if (subtitle != null) {
                model.subtitleId = subtitle.get().getId();
            }

            return model;
        }

    }

}
