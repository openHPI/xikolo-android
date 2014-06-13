package de.xikolo.dataaccess;

import android.content.Context;
import android.content.SharedPreferences;

import de.xikolo.util.Config;

public class EnrollmentsPreferences {

    public static int ENROLLMENTS_SIZE_DEFAULT = 0;
    private static String ENROLLMENTS_SIZE = "enrollments_size";
    private Context mContext;

    public EnrollmentsPreferences(Context context) {
        super();
        this.mContext = context;
    }

    public int getEnrollmentsSize() {
        SharedPreferences sharedPref = mContext.getSharedPreferences(Config.PREF_ENROLLMENTS, Context.MODE_PRIVATE);
        return sharedPref.getInt(ENROLLMENTS_SIZE, ENROLLMENTS_SIZE_DEFAULT);
    }

    public void saveEnrollmentsSize(int size) {
        SharedPreferences sharedPref = mContext.getSharedPreferences(Config.PREF_ENROLLMENTS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(ENROLLMENTS_SIZE, size);
        editor.commit();
    }

    public void deleteEnrollmentsSize() {
        SharedPreferences sharedPref = mContext.getSharedPreferences(Config.PREF_ENROLLMENTS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }

}
