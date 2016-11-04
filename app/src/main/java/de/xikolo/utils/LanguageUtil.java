package de.xikolo.utils;

import android.content.Context;

import de.xikolo.R;

public class LanguageUtil {

    public static String languageForCode(Context context, String code) {
        switch (code) {
            case "en":
                return context.getString(R.string.lang_en);
            case "de":
                return context.getString(R.string.lang_de);
            case "cn":
                return context.getString(R.string.lang_zh);
            default:
                return code;
        }
    }

}
