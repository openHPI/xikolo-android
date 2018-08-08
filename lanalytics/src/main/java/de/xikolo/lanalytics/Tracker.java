package de.xikolo.lanalytics;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import de.xikolo.lanalytics.database.DatabaseHelper;
import de.xikolo.lanalytics.database.access.EventDataAccess;
import de.xikolo.lanalytics.network.NetworkCall;
import de.xikolo.lanalytics.parser.Parser;
import de.xikolo.lanalytics.util.Logger;
import de.xikolo.lanalytics.util.NetworkUtil;
import okhttp3.Response;

public class Tracker {

    private Context context;

    private String endpoint;

    private EventDataAccess eventDataAccess;

    private Thread networkRunner;

    private volatile boolean sending;

    private String token;

    Tracker(Context context, String endpoint, DatabaseHelper databaseHelper) {
        this.context = context;
        this.endpoint = endpoint;
        this.eventDataAccess = (EventDataAccess) databaseHelper.getDataAccess(DatabaseHelper.DataAccessType.EVENT);
    }

    public void send(final Lanalytics.Event event, String token) {
        this.token = token;

        new Thread(() -> {
            Logger.d(Lanalytics.TAG, "Track event " + event.verb);

            eventDataAccess.add(event);

            startSending();
        }).start();
    }

    public void startSending() {
        Logger.d(Lanalytics.TAG, "Trying to start NetworkRunner...");

        if (networkRunner == null || !networkRunner.isAlive()) {
            sending = true;

            networkRunner = new Thread(new NetworkRunner());
            networkRunner.start();
        } else {
            Logger.d(Lanalytics.TAG, "NetworkRunner already started");
        }
    }

    public void stopSending() {
        Logger.d(Lanalytics.TAG, "Trying to stop NetworkRunner...");

        if (networkRunner != null && networkRunner.isAlive()) {
            sending = false;

            try {
                networkRunner.join();
                Logger.d(Lanalytics.TAG, "NetworkRunner stopped");
            } catch (InterruptedException e) {
                Logger.d(Lanalytics.TAG, "NetworkRunner interrupted");
            }
        } else {
            Logger.d(Lanalytics.TAG, "NetworkRunner already stopped");
        }
    }

    private class NetworkRunner implements Runnable {

        @Override
        public void run() {
            try {
                while (sending && getCountBasedOnNetwork() > 0 && NetworkUtil.isOnline(context)) {
                    Logger.d(Lanalytics.TAG, "NetworkRunner started");

                    List<Lanalytics.Event> eventList;
                    if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.NetworkConnection.MOBILE) {
                        eventList = eventDataAccess.getTopExcludeWifiOnly(50);
                    } else {
                        eventList = eventDataAccess.getTop(50);
                    }

                    if (eventList.size() <= 0) {
                        break;
                    }

                    String json;
                    if (eventList.size() == 1) {
                        json = Parser.toJsonApi(eventList.get(0));
                    } else {
                        json = Parser.toJsonApi(eventList);
                    }

                    Response response = new NetworkCall(endpoint)
                            .authorize(token)
                            .postJsonApi(json)
                            .execute();
                    if (!response.isSuccessful()) {
                        response.body().close();
                        throw new IOException("Post Request on " + endpoint + " was not successful. Status Code " + response.code());
                    }
                    response.body().close();

                    for (Lanalytics.Event event : eventList) {
                        eventDataAccess.delete(event);
                    }
                }
            } catch (Exception e) {
                Log.e(Lanalytics.TAG, e.getMessage(), e);
            }
        }

        private int getCountBasedOnNetwork() {
            if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.NetworkConnection.MOBILE) {
                return eventDataAccess.getCountExcludeWifiOnly();
            } else {
                return eventDataAccess.getCount();
            }
        }

    }

}
