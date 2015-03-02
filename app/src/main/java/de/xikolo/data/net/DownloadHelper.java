package de.xikolo.data.net;

import android.app.Application;
import android.app.DownloadManager;
import android.net.Uri;

import de.xikolo.GlobalApplication;
import de.xikolo.data.preferences.AppPreferences;

public class DownloadHelper {
    
    public static long request(String uri, String target, String title) {
        DownloadManager dm = (DownloadManager) GlobalApplication.getInstance().getSystemService(Application.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));

        if (AppPreferences.isDownloadNetworkLimitedOnMobile(GlobalApplication.getInstance())) {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        } else {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        }

        request.setDestinationUri(Uri.parse(target));
        request.setVisibleInDownloadsUi(true);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(title);
        
        return dm.enqueue(request);
    }
    
}
