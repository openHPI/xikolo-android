package de.xikolo.models;


import io.realm.RealmObject;

public interface RealmAdapter<T extends RealmObject> {

    public T convertToRealmObject();

}
