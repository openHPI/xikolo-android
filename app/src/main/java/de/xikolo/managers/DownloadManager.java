package de.xikolo.managers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.config.Config;
import de.xikolo.events.DownloadDeletedEvent;
import de.xikolo.events.DownloadStartedEvent;
import de.xikolo.events.PermissionDeniedEvent;
import de.xikolo.events.PermissionGrantedEvent;
import de.xikolo.models.Download;
import de.xikolo.services.DownloadService;
import de.xikolo.utils.DownloadUtil;
import de.xikolo.utils.ExternalStorageUtil;
import de.xikolo.utils.FileUtil;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.ToastUtil;

public class DownloadManager {

    public static final String TAG = DownloadManager.class.getSimpleName();

    private PermissionManager permissionManager;

    public enum PendingAction {
        START, DELETE, CANCEL;

        public DownloadUtil.AssetDownload download;

        public void savePayload(DownloadUtil.AssetDownload download) {
            this.download = download;
        }

    }

    private PendingAction pendingAction;

    public DownloadManager(FragmentActivity activity) {
        super();

        this.permissionManager = new PermissionManager(activity);
        this.pendingAction = null;

        EventBus.getDefault().register(this);
    }

    public boolean startAssetDownload(DownloadUtil.AssetDownload download) {
        if (ExternalStorageUtil.isExternalStorageWritable()) {

            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {

                Context context = App.getInstance();
                Intent intent = new Intent(context, DownloadService.class);

                if (downloadExists(download)) {
                    ToastUtil.show(R.string.toast_file_already_downloaded);
                    return false;
                } else if (download.getUrl() != null) {
                    FileUtil.createFolderIfNotExists(new File(download.getFilePath().substring(0, download.getFilePath().lastIndexOf(File.separator))));

                    Bundle bundle = new Bundle();
                    bundle.putString(DownloadService.ARG_TITLE, download.getTitle());
                    bundle.putString(DownloadService.ARG_URL, download.getUrl());
                    bundle.putString(DownloadService.ARG_FILE_PATH, download.getFilePath());

                    intent.putExtras(bundle);
                    context.startService(intent);

                    if (download instanceof DownloadUtil.AssetDownload.Course.Item) {
                        LanalyticsUtil.trackDownloadedFile((DownloadUtil.AssetDownload.Course.Item) download);
                    }

                    EventBus.getDefault().post(new DownloadStartedEvent(download));

                    return true;
                } else {
                    Log.i(TAG, "URL is null, nothing to download");
                    return false;
                }
            } else {
                pendingAction = PendingAction.START;
                pendingAction.savePayload(download);
                return false;
            }
        } else {
            Log.w(TAG, "No write access for external storage");
            ToastUtil.show(R.string.toast_no_external_write_access);
            return false;
        }
    }

    public boolean deleteItemAssetDownload(DownloadUtil.AssetDownload download) {
        if (ExternalStorageUtil.isExternalStorageWritable()) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {

                if (Config.DEBUG) {
                    Log.d(TAG, "Delete download " + download.getFilePath());
                }

                if (!downloadExists(download)) {
                    return false;
                } else {
                    EventBus.getDefault().post(new DownloadDeletedEvent(download));
                    File dlFile = new File(download.getFilePath());
                    return dlFile.delete();
                }
            } else {
                pendingAction = PendingAction.DELETE;
                pendingAction.savePayload(download);
                return false;
            }
        } else {
            Log.w(TAG, "No write access for external storage");
            ToastUtil.show(R.string.toast_no_external_write_access);
            return false;
        }
    }

    public void cancelItemAssetDownload(DownloadUtil.AssetDownload download) {
        if (ExternalStorageUtil.isExternalStorageWritable()) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {

                if (Config.DEBUG) {
                    Log.d(TAG, "Cancel download " + download.getUrl());
                }

                DownloadService downloadService = DownloadService.getInstance();
                if (downloadService != null) {
                    downloadService.cancelDownload(download.getUrl());
                }

                deleteItemAssetDownload(download);
            } else {
                pendingAction = PendingAction.CANCEL;
                pendingAction.savePayload(download);
            }
        } else {
            Log.w(TAG, "No write access for external storage");
            ToastUtil.show(R.string.toast_no_external_write_access);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPermissionGrantedEvent(PermissionGrantedEvent permissionGrantedEvent) {
        if (permissionGrantedEvent.getRequestCode() == PermissionManager.REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (pendingAction != null) {
                switch (pendingAction) {
                    case START:
                        startAssetDownload(pendingAction.download);
                        break;
                    case DELETE:
                        deleteItemAssetDownload(pendingAction.download);
                        break;
                    case CANCEL:
                        cancelItemAssetDownload(pendingAction.download);
                        break;
                    default:
                        pendingAction = null;
                }
            }
            pendingAction = null;
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPermissionDeniedEvent(PermissionDeniedEvent permissionDeniedEvent) {
        if (permissionDeniedEvent.getRequestCode() == PermissionManager.REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            pendingAction = null;
        }
    }

    public Download getDownload(DownloadUtil.AssetDownload download) {
        DownloadService downloadService = DownloadService.getInstance();

        Download dl = null;

        if (downloadService != null) {
            dl = downloadService.getDownload(download.getUrl());
        }

        return dl;
    }

    public boolean downloadRunning(DownloadUtil.AssetDownload download) {
        return downloadRunning(download.getUrl());
    }

    public boolean downloadRunning(String url) {
        DownloadService downloadService = DownloadService.getInstance();

        boolean running = false;

        if (downloadService != null) {
            running = downloadService.isDownloading(url);
        }

        return running;
    }

    public boolean downloadExists(DownloadUtil.AssetDownload download) {
        File file = new File(download.getFilePath());
        return file.isFile() && file.exists();
    }

    public File getDownloadFile(DownloadUtil.AssetDownload download) {
        File file = new File(download.getFilePath());

        if (file.isFile() && file.exists()) {
            return file;
        }
        return null;
    }

    public List<String> getFoldersWithDownloads() {
        List<String> folders = new ArrayList<>();

        File publicAppFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
            + App.getInstance().getString(R.string.app_name));

        if (publicAppFolder.isDirectory()) {
            File[] files = publicAppFolder.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    folders.add(file.getAbsolutePath());
                }
            }
        }

        return folders;
    }
}
