package de.xikolo.util;

import android.content.Context;

import de.xikolo.R;

public class LanguageUtil {

    public static String languageForCode(Context context, String code) {
        if (code.equals("en")) {
            return context.getString(R.string.lang_en);
        } else if (code.equals("de")) {
            return context.getString(R.string.lang_de);
        } else {
            return null;
        }
    }

}
