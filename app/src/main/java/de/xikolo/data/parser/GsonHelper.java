package de.xikolo.data.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonHelper {

    public static Gson create() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(AutoValueTypeAdapterFactory.create())
                .create();
    }

}
