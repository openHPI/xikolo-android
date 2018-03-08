package de.xikolo.models;

import de.xikolo.models.base.RealmAdapter;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import moe.banana.jsonapi2.JsonApi;
import moe.banana.jsonapi2.Resource;

public class Channel extends RealmObject {

    @PrimaryKey
    public String id;

    public String name;

    public String slug;

    public String color;

    public int position;

    public static Channel get(String id) {
        Realm realm = Realm.getDefaultInstance();
        Channel model = realm.where(Channel.class).equalTo("id", id).findFirst();
        if (model != null) model = realm.copyFromRealm(model);
        realm.close();
        return model;
    }

    @JsonApi(type = "channels")
    public static class JsonModel extends Resource implements RealmAdapter<Channel> {

        public String name;

        public String slug;

        public String color;

        public int position;

        @Override
        public Channel convertToRealmObject() {
            Channel model = new Channel();
            model.id = getId();
            model.name = name;
            model.slug = slug;
            model.color = color;
            model.position = position;

            return model;
        }

    }

}
