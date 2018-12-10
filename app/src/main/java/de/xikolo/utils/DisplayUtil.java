package de.xikolo.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class DisplayUtil {

    public static boolean is7inchTablet(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        return dpHeight >= 600 || dpWidth >= 600;
    }

    public static boolean is10inchTablet(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        return dpHeight >= 720 || dpWidth >= 720;
    }

}
