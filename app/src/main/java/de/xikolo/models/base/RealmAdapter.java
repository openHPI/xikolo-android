package de.xikolo.models.base;


import io.realm.RealmObject;

public interface RealmAdapter<T extends RealmObject> {

    public T convertToRealmObject();

}
