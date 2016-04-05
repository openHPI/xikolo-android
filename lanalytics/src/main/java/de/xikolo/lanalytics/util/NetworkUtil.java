package de.xikolo.lanalytics.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

import de.xikolo.lanalytics.Lanalytics;

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

    public static String getIpAddress(Context context) {
        String ip = null; // no network

        switch (getConnectivityStatus(context)) {
            case WIFI:
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
                ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
                break;
            case MOBILE:
                try {
                    List<NetworkInterface> interfaceList = Collections.list(NetworkInterface.getNetworkInterfaces());
                    for (NetworkInterface networkInterface : interfaceList) {
                        List<InetAddress> internetAddressList = Collections.list(networkInterface.getInetAddresses());
                        for (InetAddress internetAddress : internetAddressList) {
                            if (!internetAddress.isLoopbackAddress()) {
                                ip = internetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(Lanalytics.class.getSimpleName(), "Unable to fetch mobile network IP address", e);
                }
                break;
        }

        return ip;
    }

}
