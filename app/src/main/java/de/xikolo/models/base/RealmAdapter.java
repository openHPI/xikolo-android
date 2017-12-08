package de.xikolo.models.base;


import io.realm.RealmModel;

public interface RealmAdapter<T extends RealmModel> {

    T convertToRealmObject();

}
