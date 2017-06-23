package de.xikolo.models;

import com.squareup.moshi.Json;

import java.util.Map;

public class WebSocketMessage {

    public String platform;

    @Json(name = "client_id")
    public String clientId;

    public String action;

    public Map<String, String> payload;

}
