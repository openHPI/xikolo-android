package de.xikolo.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
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
import com.coolerfall.download.OkHttpDownloader;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.config.Config;
import de.xikolo.models.Download2;
import de.xikolo.utils.FileUtil;
import de.xikolo.utils.NotificationUtil;

public class DownloadService extends Service {

    private static final String TAG = DownloadService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 420;

    private Looper serviceLooper;

    private ServiceHandler serviceHandler;

    private NotificationUtil notificationUtil;

    private DownloadManager downloadClient;

    private ConcurrentHashMap<Integer, Download2> downloadMap;

    @Override
    public void onCreate() {
        notificationUtil = new NotificationUtil(this);

        downloadClient = new DownloadManager.Builder()
                .context(this)
                .downloader(OkHttpDownloader.create())
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
        if (Config.DEBUG) Log.d(TAG, "DownloadService destroyed");

        downloadClient.cancelAll();
        stopForeground(true);
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            final int messageId = message.arg1;

            String destPath = Environment.getExternalStorageDirectory() + File.separator + App.getInstance().getString(R.string.app_name);
            destPath += "/In-Memory_Data_Management_2017_d013d1a5-0f2c-42b7-a5ad-2bf665585faf/Week_1_b3bafe2e-faea-48e7-8218-c2a7ce4aa6ce/History_of_Enterprise_Computing_3975bbe0-01eb-456f-a465-391fd65a4286/";
            String destFilePath = destPath + "History_of_Enterprise_Computing_video_sd.mp4";

//            destPath += "/Web-Technologien_71934f16-ffeb-4abd-a256-c8818fbc567e/Woche_5_77f10153-62b6-40a8-bf33-1cb5cd7de686/5.1_Technologien_1fdb48ad-d305-4eac-954e-e31762a3c299/";
//            String destFilePath = destPath + "5.1_Technologien_slides.pdf";

            FileUtil.createFolderIfNotExists(new File(destPath));

            final String url = "https://player.vimeo.com/external/104610371.sd.mp4?s=bd16c26c117f94e5a62eab7c17295cc67e794eb8&profile_id=165&oauth2_token_id=60919992";

            DownloadRequest request = new DownloadRequest.Builder()
                    .url(url)
//                    .url("https://open.hpi.de/files/7f36c275-ecac-41e2-a01d-1d652d7e945b.pdf")
                    .retryTime(0)
                    .progressInterval(1, TimeUnit.SECONDS)
                    .allowedNetworkTypes(DownloadRequest.NETWORK_MOBILE)
                    .destinationFilePath(destFilePath)
                    .downloadCallback(new DownloadCallback() {
                        @Override
                        public void onStart(int downloadId, long totalBytes) {
                            if (Config.DEBUG) Log.i(TAG, "Download started: " + url);
                            Notification notification = notificationUtil.getDownloadsNotification()
                                    .setProgress(100, 0, false)
                                    .build();
                            notificationUtil.notify(NOTIFICATION_ID, notification);
                        }

                        @Override
                        public void onProgress(int downloadId, long bytesWritten, long totalBytes) {
                            Notification notification = notificationUtil.getDownloadsNotification()
                                    .setProgress(100, (int) (bytesWritten / (totalBytes / 100.)), false)
                                    .build();
                            notificationUtil.notify(NOTIFICATION_ID, notification);
                        }

                        @Override
                        public void onSuccess(int downloadId, String filePath) {
                            if (Config.DEBUG) Log.i(TAG, "Download finished: " + filePath);
                            // Stop the service using the startId, so that we don't stop
                            // the service in the middle of handling another job
                            DownloadService.this.stopSelf(messageId);
                        }

                        @Override
                        public void onFailure(int downloadId, int statusCode, String errMsg) {
                            Log.e(TAG, "Download failed: " + errMsg);
                            // Stop the service using the startId, so that we don't stop
                            // the service in the middle of handling another job
                            DownloadService.this.stopSelf(messageId);
                        }
                    })
                    .build();

            int downloadId = downloadClient.add(request);

        }

    }

}
