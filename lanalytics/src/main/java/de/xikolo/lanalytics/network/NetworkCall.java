package de.xikolo.lanalytics.network;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkCall {

    private Request.Builder builder;

    private static OkHttpClient httpClient;

    public static OkHttpClient getDefaultHttpClient() {
        synchronized (NetworkCall.class) {
            if (httpClient == null) {
                httpClient = new OkHttpClient();
            }
        }
        return httpClient;
    }

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public NetworkCall(String url) {
        builder = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.xikolo.v1, application/json")
                .addHeader("User-Platform", "Android");
    }

    public NetworkCall authorize(String token) {
        builder.addHeader("Authorization", "Token token=" + token);
        return this;
    }

    public NetworkCall get() {
        builder.get();
        return this;
    }

    public NetworkCall postJson(String json) {
        RequestBody body = RequestBody.create(JSON, json);
        builder.post(body);
        return this;
    }

    private Request build() {
        return builder.build();
    }

    public Response execute() throws IOException  {
        return getDefaultHttpClient().newCall(build()).execute();
    }

}
