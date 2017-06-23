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

    public String itemId;

    @JsonApi(type = "peer-assessments")
    public static class JsonModel extends Resource implements RealmAdapter<PeerAssessment> {

        public String title;

        public String itemId;

        @Override
        public PeerAssessment convertToRealmObject() {
            PeerAssessment peer = new PeerAssessment();
            peer.id = getId();
            peer.title = title;
            peer.itemId = itemId;

            return peer;
        }

    }

}
