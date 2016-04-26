package de.xikolo.lanalytics.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import de.xikolo.lanalytics.Lanalytics;

public class EventSerializer implements JsonSerializer<Lanalytics.Event> {

    @Override
    public JsonElement serialize(Lanalytics.Event src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject event = new JsonObject();
        event.addProperty("type", "lanalytics-event");

        JsonObject attributes = new JsonObject();

        JsonObject user = new JsonObject();
        user.addProperty("resource_uuid", src.userId);
        attributes.add("user", user);

        attributes.addProperty("verb", src.verb);

        JsonObject resource = new JsonObject();
        resource.addProperty("resource_uuid", src.resourceId);
        attributes.add("resource", resource);

        attributes.addProperty("timestamp", src.timestamp);

        attributes.add("with_result", context.serialize(src.result));

        attributes.add("in_context", context.serialize(src.context));

        event.add("attributes", attributes);

        return event;
    }
}
