package de.xikolo.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.coolerfall.download.DownloadCallback;
import com.coolerfall.download.DownloadManager;
import com.coolerfall.download.DownloadRequest;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import de.xikolo.config.Config;
import de.xikolo.models.Download;
import de.xikolo.storages.ApplicationPreferences;
import de.xikolo.utils.NotificationUtil;

public class DownloadService extends Service {

    public static final String TAG = DownloadService.class.getSimpleName();

    public static final String ARG_TITLE = "title";

    public static final String ARG_URL = "url";

    public static final String ARG_FILE_PATH = "file_path";

    private static final int NOTIFICATION_ID = 420;

    private Looper serviceLooper;

    private ServiceHandler serviceHandler;

    private NotificationUtil notificationUtil;

    private DownloadManager downloadClient;

    private ConcurrentHashMap<Integer, Download> downloadMap;

    private static DownloadService instance;

    public static DownloadService getInstance() {
        return instance;
    }

    public synchronized boolean isDownloading(String url) {
        return downloadClient != null && downloadClient.isDownloading(url);
    }

    public synchronized Download getDownload(String url) {
        for (Download download : downloadMap.values()) {
            if (url.equals(download.url)) return download;
        }

        return null;
    }

    public synchronized void cancelDownload(String url) {
        Download download = getDownload(url);
        if (download != null && isDownloading(url)) {
            downloadClient.cancel(download.id);
        }
    }

    private synchronized void updateDownloadProgress(int downloadId, long bytesWritten, long totalBytes) {
        Download download = downloadMap.get(downloadId);

        if (download != null) {
            download.state = Download.State.RUNNING;
            download.bytesWritten = bytesWritten;
            download.totalBytes = totalBytes;

            downloadMap.put(download.id, download);
        }

        long bytesWrittenOfAll = 0;
        long totalBytesOfAll = 0;

        for (Download d : downloadMap.values()) {
            bytesWrittenOfAll += d.bytesWritten;
            totalBytesOfAll += d.totalBytes;
        }

        Notification notification = notificationUtil.getDownloadsNotification()
                .setProgress(100, (int) (bytesWrittenOfAll / (totalBytesOfAll / 100.)), false)
                .build();
        notificationUtil.notify(NOTIFICATION_ID, notification);
    }

    private synchronized void updateDownloadSuccess(int downloadId) {
        Download download = downloadMap.get(downloadId);

        if (download != null) {
            download.state = Download.State.SUCCESSFUL;
        }
    }

    private synchronized void updateDownloadFailure(int downloadId) {
        Download download = downloadMap.get(downloadId);

        if (download != null) {
            download.state = Download.State.FAILURE;
        }
    }

    @Override
    public void onCreate() {
        instance = this;

        notificationUtil = new NotificationUtil(this);

        downloadClient = new DownloadManager.Builder()
                .context(this)
//                .downloader(OkHttpDownloader.create())
                .build();

        downloadMap = new ConcurrentHashMap<>();

        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "DownloadService start command received");

        Notification notification = notificationUtil.getDownloadsNotification().build();
        startForeground(NOTIFICATION_ID, notification);

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.setData(intent.getExtras());
        serviceHandler.sendMessage(msg);

        Log.w(TAG, "Start ServiceHandler with ID: " + msg.arg1);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        instance = null;

        if (Config.DEBUG) Log.d(TAG, "DownloadService destroyed");

        stopForeground(true);
        downloadClient.cancelAll();
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            final int messageId = message.arg1;

            final String title = message.getData().getString(ARG_TITLE);
            final String url = message.getData().getString(ARG_URL);
            final String filePath = message.getData().getString(ARG_FILE_PATH);

            int allowedNetworkTypes = DownloadRequest.NETWORK_WIFI | DownloadRequest.NETWORK_MOBILE;
            ApplicationPreferences appPreferences = new ApplicationPreferences();
            if (appPreferences.isDownloadNetworkLimitedOnMobile()) {
                allowedNetworkTypes = DownloadRequest.NETWORK_WIFI;
            }

            DownloadRequest request = new DownloadRequest.Builder()
                    .url(url)
                    .retryTime(0)
                    .progressInterval(1, TimeUnit.SECONDS)
                    .allowedNetworkTypes(allowedNetworkTypes)
                    .destinationFilePath(filePath)
                    .downloadCallback(new DownloadCallback() {
                        @Override
                        public void onStart(int downloadId, long totalBytes) {
                            if (Config.DEBUG) Log.i(TAG, "Download started: " + url);

                            updateDownloadProgress(downloadId, 0, totalBytes);
                        }

                        @Override
                        public void onProgress(int downloadId, long bytesWritten, long totalBytes) {
                            updateDownloadProgress(downloadId, bytesWritten, totalBytes);
                        }

                        @Override
                        public void onSuccess(int downloadId, String filePath) {
                            if (Config.DEBUG) Log.i(TAG, "Download finished: " + filePath);

                            updateDownloadSuccess(downloadId);

                            // Stop the service using the startId, so that we don't stop
                            // the service in the middle of handling another job
                            DownloadService.this.stopSelf(messageId);
                        }

                        @Override
                        public void onFailure(int downloadId, int statusCode, String errMsg) {
                            Log.e(TAG, "Download failed: " + errMsg);

                            updateDownloadFailure(downloadId);

                            // Stop the service using the startId, so that we don't stop
                            // the service in the middle of handling another job
                            DownloadService.this.stopSelf(messageId);
                        }
                    })
                    .build();

            Download download = new Download();
            download.title = title;
            download.url = url;
            download.filePath = filePath;
            download.id = downloadClient.add(request);

            downloadMap.putIfAbsent(download.id, download);
        }

    }

}
