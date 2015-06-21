package de.xikolo.data.net;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;

import java.io.File;
import java.util.ArrayList;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.data.entities.Download;
import de.xikolo.util.NotificationProgressUtil;

public class DownloadHelper {

    private static DownloadHelper instance;

    private ArrayList<Download> downloads;
    private CompletedCallback completedCallback;

    private DownloadHelper() {
        downloads = new ArrayList<>();
    }

    public static DownloadHelper getInstance() {
        if (instance == null) {
            instance = new DownloadHelper();
        }
        return instance;
    }

    public ArrayList<Download> getDownloads() {
        return downloads;
    }

    public void setCompletedCallback(CompletedCallback completedCallback) {
        this.completedCallback = completedCallback;
    }

    public void request(String uri, String target, String title) {
        final int notificationIndex = NotificationProgressUtil.getInstance()
                .show(GlobalApplication.getInstance().getString(R.string.downloading_title),
                        title, R.drawable.ic_launcher);
        final Download download = new Download(title, target, uri);
        download.setFileFuture(Ion.with(GlobalApplication.getInstance())
                .load(uri)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        download.status = Download.STATUS_RUNNING;
                        download.bytesDownloadedSoFar = downloaded;
                        download.totalSizeBytes = total;
                        NotificationProgressUtil.getInstance()
                                .setProgress(notificationIndex, (int) downloaded, (int) total);
                    }
                })
                .write(new File(target))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File file) {
                        String downloadMessage = GlobalApplication.getInstance().
                                getString(R.string.downloading_title_completed);
                        if (e != null) {
                            // TODO: handle exception if needed.
                            download.status = Download.STATUS_FAILED;
                            downloadMessage = GlobalApplication.getInstance().
                                    getString(R.string.downloading_title_error);
                            completedCallback.onCompleted(null);
                        } else if (completedCallback != null) {
                            download.status = Download.STATUS_SUCCESSFUL;
                            completedCallback.onCompleted(download);
                        }
                        NotificationProgressUtil.getInstance().onCompleted(notificationIndex, downloadMessage);
                        downloads.remove(download);
                    }
                }));
        downloads.add(download);
    }

    public void remove(Download download) {
        download.cancel();
        downloads.remove(download);
    }

    public boolean isRunning(String fileName) {
        for (Download download : downloads) {
            if (download.localFilename.equals(fileName) && download.status == Download.STATUS_RUNNING) {
                return true;
            }
        }
        return false;
    }

    public interface CompletedCallback {
        void onCompleted(Download download);
    }
}
