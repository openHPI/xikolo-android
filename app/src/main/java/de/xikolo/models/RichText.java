package de.xikolo.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class RichText extends RealmObject {

    @PrimaryKey
    public String id;

    public String text;

    public String itemId;

    @JsonApi(type = "rich-texts")
    public static class JsonModel extends Resource implements RealmAdapter<RichText> {

        public String text;

        public String itemId;

        @Override
        public RichText convertToRealmObject() {
            RichText rt = new RichText();
            rt.id = getId();
            rt.text = text;
            rt.itemId = itemId;

            return rt;
        }

    }

}
