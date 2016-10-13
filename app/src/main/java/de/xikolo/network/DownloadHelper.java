package de.xikolo.network;

import android.app.Application;
import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;

import java.util.HashSet;
import java.util.Set;

import de.xikolo.GlobalApplication;
import de.xikolo.models.Download;
import de.xikolo.storages.preferences.ApplicationPreferences;
import de.xikolo.storages.preferences.StorageType;

public class DownloadHelper {

    public static long request(String uri, String target, String title) {
        DownloadManager dm = (DownloadManager) GlobalApplication.getInstance().getSystemService(Application.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(uri));

        ApplicationPreferences appPreferences = (ApplicationPreferences) GlobalApplication.getStorage(StorageType.APP);

        if (appPreferences.isDownloadNetworkLimitedOnMobile()) {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        } else {
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        }

        request.setDestinationUri(Uri.parse(target));
        request.setVisibleInDownloadsUi(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle(title);

        return dm.enqueue(request);
    }

    public static int remove(long... ids) {
        DownloadManager dm = (DownloadManager) GlobalApplication.getInstance().getSystemService(Application.DOWNLOAD_SERVICE);
        return dm.remove(ids);
    }

    public static Download buildDownloadObject(Cursor c) {
        long id = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID));
        String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
        String description = c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION));
        String localUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
        String uri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI));
        int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
        int reason = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON));
        long totalSizeBytes = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
        long bytesDownloadedSoFar = c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
        long lastModifiedTimestamp = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP));
        String mediaproviderUri = c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIAPROVIDER_URI));
        String mediaType = c.getString(c.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE));

        return new Download(id, title, description, localUri, uri, status, reason,
                totalSizeBytes, bytesDownloadedSoFar, lastModifiedTimestamp, mediaproviderUri, mediaType);
    }

    public static Set<Download> getAllDownloads() {
        Set<Download> downloadSet = new HashSet<>();
        DownloadManager dm = (DownloadManager) GlobalApplication.getInstance().getSystemService(Application.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        Cursor c = dm.query(query);
        if (c.moveToFirst()) {
            do {
                Download dl = buildDownloadObject(c);
                downloadSet.add(dl);
            } while (c.moveToNext());
        }
        c.close();
        return downloadSet;
    }

    public static Download getDownloadForId(long id) {
        Download dl = null;
        DownloadManager dm = (DownloadManager) GlobalApplication.getInstance().getSystemService(Application.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(id);
        Cursor c = dm.query(query);
        if (c.moveToFirst()) {
            dl = buildDownloadObject(c);
        }
        c.close();
        return dl;
    }

    public static Set<Download> getAllDownloadsForIds(long... ids) {
        Set<Download> downloadSet = new HashSet<>();
        DownloadManager dm = (DownloadManager) GlobalApplication.getInstance().getSystemService(Application.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(ids);
        Cursor c = dm.query(query);
        if (c.moveToFirst()) {
            do {
                Download dl = buildDownloadObject(c);
                downloadSet.add(dl);
            } while (c.moveToNext());
        }
        c.close();
        return downloadSet;
    }

    public static Set<Download> getAllDownloadsForStatus(int flags) {
        Set<Download> downloadSet = new HashSet<>();
        DownloadManager dm = (DownloadManager) GlobalApplication.getInstance().getSystemService(Application.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterByStatus(flags);
        Cursor c = dm.query(query);
        if (c.moveToFirst()) {
            do {
                Download dl = buildDownloadObject(c);
                downloadSet.add(dl);
            } while (c.moveToNext());
        }
        c.close();
        return downloadSet;
    }

}
