package de.xikolo.network;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkRequest {

    protected Request.Builder builder;

    protected static OkHttpClient httpClient;

    private Response response;

    protected static OkHttpClient getDefaultHttpClient() {
        synchronized (NetworkRequest.class) {
            if (httpClient == null) {
                httpClient = new OkHttpClient();
            }
        }
        return httpClient;
    }

    public NetworkRequest(String url) {
        builder = new Request.Builder()
                .url(url);
    }

    public NetworkRequest get() {
        builder.get();
        return this;
    }

    public NetworkRequest post() {
        return post(null);
    }

    public NetworkRequest post(RequestBody body) {
        builder.post(body);
        return this;
    }

    public NetworkRequest put() {
        return put(null);
    }

    public NetworkRequest put(RequestBody body) {
        builder.put(body);
        return this;
    }

    public NetworkRequest delete() {
        builder.delete();
        return this;
    }

    public NetworkRequest head() {
        builder.head();
        return this;
    }

    private Request build() {
        return builder.build();
    }

    public Response execute() throws IOException  {
        return response = getDefaultHttpClient().newCall(build()).execute();
    }

    public void close() {
        if (response != null) {
            response.close();
        }
    }

}
