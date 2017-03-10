package de.xikolo.storages.preferences;

import android.content.SharedPreferences;

import de.xikolo.GlobalApplication;

public abstract class BaseStorage {

    protected SharedPreferences preferences;

    BaseStorage(String name, int mode) {
        this.preferences = GlobalApplication.getInstance().getSharedPreferences(name, mode);
    }

}
