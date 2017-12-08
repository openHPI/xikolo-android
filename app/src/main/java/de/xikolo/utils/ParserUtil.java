package de.xikolo.utils;

import android.util.Log;

import com.squareup.moshi.Moshi;

import java.io.IOException;

public class ParserUtil {

    public static final String TAG = ParserUtil.class.getSimpleName();

    public static Moshi create() {
        return new Moshi.Builder().build();
    }

    public static <T> T parse(String json, Class<T> clazz) {
        try {
            return create().adapter(clazz).fromJson(json);
        } catch(IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

}
