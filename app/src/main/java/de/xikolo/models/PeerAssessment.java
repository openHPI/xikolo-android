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

    @JsonApi(type = "peer-assessments")
    public static class JsonModel extends Resource implements RealmAdapter<PeerAssessment> {

        public String title;

        @Override
        public PeerAssessment convertToRealmObject() {
            PeerAssessment peer = new PeerAssessment();
            peer.id = getId();
            peer.title = title;

            return peer;
        }

    }

}
