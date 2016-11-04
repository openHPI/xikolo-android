package de.xikolo.storages.preferences;

import android.content.Context;

public class StorageHelper {

    private Context context;

    public StorageHelper(Context context) {
        this.context = context;
    }

    public KeyValueStorage getStorage(StorageType type) {
        KeyValueStorage storage = null;

        switch (type) {
            case APP:
                return new ApplicationPreferences(context);
            case USER:
                return new UserStorage(context);
            case NOTIFICATION:
                return new NotificationStorage(context);
        }

        return storage;
    }

}
