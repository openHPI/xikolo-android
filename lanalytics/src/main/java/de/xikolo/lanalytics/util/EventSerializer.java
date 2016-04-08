package de.xikolo.lanalytics.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import de.xikolo.lanalytics.Lanalytics;

public class EventSerializer implements JsonSerializer<Lanalytics.Event> {

    @Override
    public JsonElement serialize(Lanalytics.Event src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();

        JsonObject user = new JsonObject();
        user.addProperty("resource_uuid", src.userId);
        jsonObject.add("user", user);

        jsonObject.addProperty("verb", src.verb);

        JsonObject resource = new JsonObject();
        resource.addProperty("resource_uuid", src.resourceId);
        jsonObject.add("resource", resource);

        jsonObject.addProperty("timestamp", src.timestamp);

        jsonObject.add("with_result", context.serialize(src.result));

        jsonObject.add("in_context", context.serialize(src.context));

        return jsonObject;
    }
}
