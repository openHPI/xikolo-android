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
import de.xikolo.models.Course;
import de.xikolo.models.Download;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.Video;
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

        public String itemId;
        public DownloadUtil.VideoAssetType type;

        public void savePayload(String itemId, DownloadUtil.VideoAssetType type) {
            this.itemId = itemId;
            this.type = type;
        }

    }

    private PendingAction pendingAction;

    public DownloadManager(FragmentActivity activity) {
        super();

        this.permissionManager = new PermissionManager(activity);
        this.pendingAction = null;

        EventBus.getDefault().register(this);
    }

    public boolean startItemAssetDownload(String itemId, DownloadUtil.VideoAssetType type) {
        if (ExternalStorageUtil.isExternalStorageWritable()) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {

                Context context = App.getInstance();
                Intent intent = new Intent(context, DownloadService.class);

                Item item = Item.get(itemId);
                Section section = item.getSection();
                Course course = section.getCourse();

                String url = getDownloadUrl(itemId, type);
                String filePath = DownloadUtil.getVideoAssetFilePath(type, course, section, item);

                if (downloadExists(filePath)) {
                    ToastUtil.show(R.string.toast_file_already_downloaded);
                    return false;
                } else if (url != null) {
                    FileUtil.createFolderIfNotExists(new File(filePath.substring(0, filePath.lastIndexOf(File.separator))));

                    Bundle bundle = new Bundle();
                    bundle.putString(DownloadService.ARG_TITLE, item.title + " (" + type.toString() + ")");
                    bundle.putString(DownloadService.ARG_URL, url);
                    bundle.putString(DownloadService.ARG_FILE_PATH, filePath);

                    intent.putExtras(bundle);
                    context.startService(intent);

                    LanalyticsUtil.trackDownloadedFile(item.id, course.id, section.id, type);

                    EventBus.getDefault().post(new DownloadStartedEvent(itemId, type));

                    return true;
                } else {
                    Log.i(TAG, "URL is null, nothing to download");
                    return false;
                }
            } else {
                pendingAction = PendingAction.START;
                pendingAction.savePayload(itemId, type);
                return false;
            }
        } else {
            Log.w(TAG, "No write access for external storage");
            ToastUtil.show(R.string.toast_no_external_write_access);
            return false;
        }
    }

    public boolean deleteItemAssetDownload(String itemId, DownloadUtil.VideoAssetType type) {
        if (ExternalStorageUtil.isExternalStorageWritable()) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {

                Item item = Item.get(itemId);
                Section section = item.getSection();
                Course course = section.getCourse();

                String filePath = DownloadUtil.getVideoAssetFilePath(type, course, section, item);

                if (Config.DEBUG) {
                    Log.d(TAG, "Delete download " + filePath);
                }

                if (!downloadExists(filePath)) {
                    return false;
                } else {
                    EventBus.getDefault().post(new DownloadDeletedEvent(itemId, type));
                    File dlFile = new File(filePath);
                    return dlFile.delete();
                }
            } else {
                pendingAction = PendingAction.DELETE;
                pendingAction.savePayload(itemId, type);
                return false;
            }
        } else {
            Log.w(TAG, "No write access for external storage");
            ToastUtil.show(R.string.toast_no_external_write_access);
            return false;
        }
    }

    public void cancelItemAssetDownload(String itemId, DownloadUtil.VideoAssetType type) {
        if (ExternalStorageUtil.isExternalStorageWritable()) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {

                String url = getDownloadUrl(itemId, type);

                if (Config.DEBUG) {
                    Log.d(TAG, "Cancel download " + url);
                }

                DownloadService downloadService = DownloadService.getInstance();
                if (downloadService != null) {
                    downloadService.cancelDownload(url);
                }

                deleteItemAssetDownload(itemId, type);
            } else {
                pendingAction = PendingAction.CANCEL;
                pendingAction.savePayload(itemId, type);
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
                        startItemAssetDownload(pendingAction.itemId, pendingAction.type);
                        break;
                    case DELETE:
                        deleteItemAssetDownload(pendingAction.itemId, pendingAction.type);
                        break;
                    case CANCEL:
                        cancelItemAssetDownload(pendingAction.itemId, pendingAction.type);
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

    public Download getDownload(String itemId, DownloadUtil.VideoAssetType type) {
        DownloadService downloadService = DownloadService.getInstance();
        String downloadUrl = getDownloadUrl(itemId, type);
        Download download = null;

        if (downloadService != null) {
            download = downloadService.getDownload(downloadUrl);
        }

        return download;
    }

    public boolean downloadRunning(String itemId, DownloadUtil.VideoAssetType type) {
        DownloadService downloadService = DownloadService.getInstance();
        String downloadUrl = getDownloadUrl(itemId, type);
        boolean running = false;

        if (downloadService != null) {
            running = downloadService.isDownloading(downloadUrl);
        }

        return running;
    }

    public boolean downloadExists(String itemId, DownloadUtil.VideoAssetType type) {
        Item item = Item.get(itemId);
        Section section = item.getSection();
        Course course = section.getCourse();

        String filePath = DownloadUtil.getVideoAssetFilePath(type, course, section, item);

        return downloadExists(filePath);
    }

    private boolean downloadExists(String filePath) {
        File file = new File(filePath);
        return file.isFile() && file.exists();
    }

    public File getDownloadFile(String itemId, DownloadUtil.VideoAssetType type) {
        Item item = Item.get(itemId);
        Section section = item.getSection();
        Course course = section.getCourse();

        String filePath = DownloadUtil.getVideoAssetFilePath(type, course, section, item);
        File file = new File(filePath);

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

    private String getDownloadUrl(String itemId, DownloadUtil.VideoAssetType type) {
        Item item = Item.get(itemId);
        Video video = Video.getForContentId(item.contentId);
        return DownloadUtil.getVideoAssetUrl(type, video);
    }

}
