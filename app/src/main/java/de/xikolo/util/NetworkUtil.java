package de.xikolo.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import de.xikolo.R;

public class NetworkUtil {

    public static int TYPE_NOT_CONNECTED = 0;
    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;

    public static boolean isOnline(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null) {
            if (netInfo.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    public static void showNoConnectionToast() {
        ToastUtil.show(R.string.toast_no_network);
    }

}
