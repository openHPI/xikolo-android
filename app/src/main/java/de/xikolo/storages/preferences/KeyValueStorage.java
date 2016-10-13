package de.xikolo.storages.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

public abstract class KeyValueStorage {

    protected Context context;

    protected SharedPreferences preferences;

    KeyValueStorage(Context context, String name, int mode) {
        this.context = context;
        this.preferences = context.getSharedPreferences(name, mode);
    }

    KeyValueStorage(Context context) {
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

}
