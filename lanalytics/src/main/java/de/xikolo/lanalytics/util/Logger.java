package de.xikolo.lanalytics.util;

import android.util.Log;

public class Logger {

    public static void d(String tag, String message) {
        if (Config.DEBUG) {
            Log.d(tag, message);
        }
    }

}
