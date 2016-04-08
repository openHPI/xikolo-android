package de.xikolo.lanalytics;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

import de.xikolo.lanalytics.database.DatabaseHelper;
import de.xikolo.lanalytics.database.access.EventDataAccess;
import de.xikolo.lanalytics.util.EventSerializer;
import de.xikolo.lanalytics.util.Logger;
import de.xikolo.lanalytics.util.NetworkUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Tracker {

    private Context context;

    private EventDataAccess eventDataAccess;

    private OkHttpClient httpClient;

    private Thread networker;

    Tracker(Context context, DatabaseHelper databaseHelper) {
        this.context = context;
        this.eventDataAccess = (EventDataAccess) databaseHelper.getDataAccess(DatabaseHelper.DataAccessType.EVENT);
        this.httpClient = new OkHttpClient();
    }

    public void track(final Lanalytics.Event event) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.d(Lanalytics.TAG, "Track event " + event.verb);

                eventDataAccess.add(event);

                if (networker == null || !networker.isAlive()) {
                    networker = new Thread(new Networker());
                    networker.start();

                    Logger.d(Lanalytics.TAG, "Start networker...");
                }
            }
        }).start();
    }

    private class Networker implements Runnable {

        @Override
        public void run() {
            try {
                while (eventDataAccess.getCount() > 0 && NetworkUtil.isOnline(context)) {
                    Logger.d(Lanalytics.TAG, "Networker started");

                    List<Lanalytics.Event> eventList = eventDataAccess.getTop(50);

                    Logger.d(Lanalytics.TAG, "Fetched events: " + eventList.size());

                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(Lanalytics.Event.class, new EventSerializer())
                            .setPrettyPrinting()
                            .create();
                    String json = gson.toJson(eventList);

                    Logger.d(Lanalytics.TAG, "JSON of events: " + json);

                    String url = "http://192.168.1.34:9000";
                    Response response = post(url, json);
                    if (!response.isSuccessful()) {
                        throw new IOException("Post Request on " + url + " was not successful. Status Code " + response.code());
                    }
                    response.body().close();

                    Logger.d(Lanalytics.TAG, "Events successfully transferred");

                    for (Lanalytics.Event event : eventList) {
                        eventDataAccess.delete(event);
                    }
                }
            } catch (IOException e) {
                Log.e(Lanalytics.TAG, e.getMessage(), e);
            }
        }

    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private Response post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", "application/vnd.xikolo.v1, application/json")
                .addHeader("Authorization", "Token token=")
                .addHeader("User-Platform", "android")
                .post(body)
                .build();

        return httpClient.newCall(request).execute();
    }

}
