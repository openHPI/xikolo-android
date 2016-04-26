package de.xikolo.lanalytics.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.xikolo.lanalytics.Lanalytics;

public class Parser {

    private static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(JsonApiWrapper.class, new JsonApiSerializer())
                .registerTypeAdapter(Lanalytics.Event.class, new EventSerializer())
                .setPrettyPrinting()
                .create();
    }

    public static String toJson(Object src) {
        return getGson().toJson(new JsonApiWrapper(src));
    }

}
