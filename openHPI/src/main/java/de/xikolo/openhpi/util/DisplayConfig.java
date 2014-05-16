package de.xikolo.openhpi.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class DisplayConfig {

    public static boolean isTablet(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        if (dpHeight >= 600 || dpWidth >= 820) {
            return true;
        } else {
            return false;
        }
    }

}
