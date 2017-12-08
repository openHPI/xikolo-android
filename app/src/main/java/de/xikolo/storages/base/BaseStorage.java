package de.xikolo.storages.base;

import android.content.SharedPreferences;

import de.xikolo.App;

public abstract class BaseStorage {

    protected SharedPreferences preferences;

    public BaseStorage(String name, int mode) {
        this.preferences = App.getInstance().getSharedPreferences(name, mode);
    }

}
