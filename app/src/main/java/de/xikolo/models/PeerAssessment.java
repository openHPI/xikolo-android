package de.xikolo.models;

import de.xikolo.models.base.RealmAdapter;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class PeerAssessment extends RealmObject {

    @PrimaryKey
    public String id;

    public String title;

    public String instructions;

    public String type;

    public static final String TYPE_SOLO = "solo";
    public static final String TYPE_TEAM = "team";

    @JsonApi(type = "peer-assessments")
    public static class JsonModel extends Resource implements RealmAdapter<PeerAssessment> {

        public String title;

        public String instructions;

        public String type;

        @Override
        public PeerAssessment convertToRealmObject() {
            PeerAssessment peer = new PeerAssessment();
            peer.id = getId();
            peer.title = title;
            peer.instructions = instructions;
            peer.type = type;

            return peer;
        }

    }

}
