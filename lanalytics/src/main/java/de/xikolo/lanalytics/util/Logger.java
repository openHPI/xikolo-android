package de.xikolo.lanalytics.util;

import android.util.Log;

import de.xikolo.lanalytics.BuildConfig;

public class Logger {

    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }
    }

}
