package de.xikolo.models;

import android.os.Parcelable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

@AutoValue
public abstract class WebSocketMessage implements Parcelable {

    @SerializedName("platform")
    public abstract String platform();

    @SerializedName("client_id")
    public abstract String clientId();

    @SerializedName("action")
    public abstract String action();

    @SerializedName("payload")
    public abstract Map<String, String> payload();

    public static WebSocketMessage create(String client, String clientId, String action, Map<String, String> payload) {
        return new AutoValue_WebSocketMessage(client, clientId, action, payload);
    }

    public static TypeAdapter<WebSocketMessage> typeAdapter(Gson gson) {
        return new AutoValue_WebSocketMessage.GsonTypeAdapter(gson);
    }

}
