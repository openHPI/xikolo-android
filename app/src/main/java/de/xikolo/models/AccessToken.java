package de.xikolo.models;

import com.squareup.moshi.Json;

public class AccessToken {

    public String token;

    @Json(name = "user_id")
    public String userId;

}
