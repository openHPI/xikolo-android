package de.xikolo.models;

import io.realm.RealmObject;

// Realm does not allow matching in queries against List<String> with `object.list` but with a List<RealmStringWrapper> and `object.list.value` it works
public class RealmStringWrapper extends RealmObject {

    public String value;
}
