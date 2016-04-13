package de.xikolo.lanalytics.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {

    public enum NetworkConnection {
        NOT_CONNECTED, WIFI, MOBILE
    }

    public static boolean isOnline(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnected();
    }

    public static NetworkConnection getConnectivityStatus(Context context) {
        if (context == null) {
            return NetworkConnection.NOT_CONNECTED;
        }
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null) {
            if (netInfo.getType() == ConnectivityManager.TYPE_WIFI)
                return NetworkConnection.WIFI;

            if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE)
                return NetworkConnection.MOBILE;
        }
        return NetworkConnection.NOT_CONNECTED;
    }

}
