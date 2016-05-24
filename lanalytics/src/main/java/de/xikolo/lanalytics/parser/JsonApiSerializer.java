package de.xikolo.lanalytics.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class JsonApiSerializer<E> implements JsonSerializer<JsonApiWrapper> {

    @Override
    public JsonElement serialize(JsonApiWrapper src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject list = new JsonObject();

        list.add("data", context.serialize(src.getPayload()));

        return list;
    }
}
