package de.xikolo.data.entities;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

@AutoValue
public abstract class WebSocketMessage implements Parcelable {

    @SerializedName("resource_id")
    public abstract String resourceId();

    @SerializedName("resource_type")
    public abstract String resourceType();

    @SerializedName("verb")
    public abstract String verb();

    @SerializedName("client_id")
    public abstract String clientId();

    @SerializedName("payload")
    public abstract Map<String, String> payload();

    public static WebSocketMessage create(String resourceId, String resourceType, String verb, String clientId, Map<String, String> payload) {
        return new AutoValue_WebSocketMessage(resourceId, resourceType, verb, clientId, payload);
    }

    public static TypeAdapter<WebSocketMessage> typeAdapter(Gson gson) {
        return new AutoValue_WebSocketMessage.GsonTypeAdapter(gson);
    }

}
