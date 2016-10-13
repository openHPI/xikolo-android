package de.xikolo.network.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.lang.reflect.Type;

import okhttp3.Response;

public class ApiParser {

    public static Gson create() {
        return new GsonBuilder()
                .registerTypeAdapterFactory(AutoValueTypeAdapterFactory.create())
                .create();
    }

    public static <T> T parse(Reader reader, Type type) {
        return create().fromJson(reader, type);
    }

    public static <T> T parse(Reader reader, Class<T> clazz) {
        return create().fromJson(reader, clazz);
    }

    public static <T> T parse(Response response, Type type) {
        return create().fromJson(response.body().charStream(), type);
    }

    public static <T> T parse(Response response, Class<T> clazz) {
        return create().fromJson(response.body().charStream(), clazz);
    }

    public static <T> T parse(String json, Type type) {
        return create().fromJson(json, type);
    }

    public static <T> T parse(String json, Class<T> clazz) {
        return create().fromJson(json, clazz);
    }

}
