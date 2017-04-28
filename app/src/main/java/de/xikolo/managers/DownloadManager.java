package de.xikolo.managers;

import android.app.Activity;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.events.DownloadDeletedEvent;
import de.xikolo.events.DownloadStartedEvent;
import de.xikolo.events.PermissionDeniedEvent;
import de.xikolo.events.PermissionGrantedEvent;
import de.xikolo.models.Course;
import de.xikolo.models.Download;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.network.DownloadHelper;
import de.xikolo.utils.Config;
import de.xikolo.utils.ExternalStorageUtil;
import de.xikolo.utils.LanalyticsUtil;
import de.xikolo.utils.ToastUtil;

public class DownloadManager extends BaseManager {

    public static final String TAG = DownloadManager.class.getSimpleName();

    private PermissionManager permissionManager;

    public enum PendingAction {
            START, DELETE, CANCEL;

        private String uri;
        private DownloadFileType type;
        private Course course;
        private Section section;
        private Item item;

        public void savePayload(String uri, DownloadFileType type, Course course, Section section, Item item) {
            this.uri = uri;
            this.type = type;
            this.course = course;
            this.section = section;
            this.item = item;
        }

        public String getUri() {
            return uri;
        }

        public DownloadFileType getType() {
            return type;
        }

        public Course getCourse() {
            return course;
        }

        public Section getSection() {
            return section;
        }

        public Item getItem() {
            return item;
        }
    }

    private PendingAction pendingAction;

    public DownloadManager(Activity activity) {
        super();

        this.permissionManager = new PermissionManager(activity);
        this.pendingAction = null;

        EventBus.getDefault().register(this);
    }

//    public void getRemoteDownloadFileSize(Result<Long> result, String url) {
//        jobManager.addJobInBackground(new RetrieveContentLengthJob(result, url));
//    }

    public long startDownload(String uri, DownloadFileType type, Course course, Section section, Item item) {
        if (Config.DEBUG) {
            Log.d(TAG, "Start download for " + uri);
        }
        if (ExternalStorageUtil.isExternalStorageWritable()) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {
                String file =  this.escapeFilename(item.title) + type.getFileSuffix();
                Uri downloadUri = buildDownloadUri(type, course, section, item);

                if (downloadExists(downloadUri)) {
                    ToastUtil.show(R.string.toast_file_already_downloaded);
                } else {
                    LanalyticsUtil.trackDownloadedFile(item.id, course.id, section.id, type);

                    File dlFile = new File(downloadUri.getPath());

                    createFolderIfNotExists(new File(dlFile.getAbsolutePath().replace(file, "")));

                    EventBus.getDefault().post(new DownloadStartedEvent(uri));
                    return DownloadHelper.request(uri, "file://" + dlFile.getAbsolutePath(), item.title + " (" + type.toString() + ")");
                }
            } else {
                pendingAction = PendingAction.START;
                pendingAction.savePayload(uri, type, course, section, item);
            }
        } else {
            Log.w(TAG, "No write access for external storage");
            ToastUtil.show(R.string.toast_no_external_write_access);
        }
        return 0;
    }

    public boolean deleteDownload(DownloadFileType type, Course course, Section section, Item item) {
        if (ExternalStorageUtil.isExternalStorageWritable()) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {
                Uri downloadUri = buildDownloadUri(type, course, section, item);

                if (Config.DEBUG) {
                    Log.d(TAG, "Delete download " + downloadUri.toString());
                }

                if (!downloadExists(downloadUri)) {
                    return false;
                } else {
                    EventBus.getDefault().post(new DownloadDeletedEvent(item));
                    File dlFile = new File(downloadUri.getPath());
                    return dlFile.delete();
                }
            } else {
                pendingAction = PendingAction.DELETE;
                pendingAction.savePayload(null, type, course, section, item);
                return false;
            }
        } else {
            Log.w(TAG, "No write access for external storage");
            ToastUtil.show(R.string.toast_no_external_write_access);
            return false;
        }
    }

    public boolean cancelDownload(DownloadFileType type, Course course, Section section, Item item) {
        if (ExternalStorageUtil.isExternalStorageWritable()) {
            if (permissionManager.requestPermission(PermissionManager.WRITE_EXTERNAL_STORAGE) == 1) {
                Uri downloadUri = buildDownloadUri(type, course, section, item);
                Download dl = new Download();
                dl.localUri = downloadUri.toString();

                if (Config.DEBUG) {
                    Log.d(TAG, "Cancel download " + downloadUri.toString());
                }

                int id = 0;
                Set<Download> dlSet = DownloadHelper.getAllDownloads();
                for (Download download : dlSet) {
                    if (download.equals(dl)) {
                        id = DownloadHelper.remove(download.id);
                    }
                }
                if (id > 0) {
                    EventBus.getDefault().post(new DownloadDeletedEvent(item));
                    return true;
                }

                return false;
            } else {
                pendingAction = PendingAction.CANCEL;
                pendingAction.savePayload(null, type, course, section, item);
                return false;
            }
        } else {
            Log.w(TAG, "No write access for external storage");
            ToastUtil.show(R.string.toast_no_external_write_access);
            return false;
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onPermissionGrantedEvent(PermissionGrantedEvent permissionGrantedEvent) {
        if (permissionGrantedEvent.getRequestCode() == PermissionManager.REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if (pendingAction != null) {
                switch (pendingAction) {
                    case START:
                        startDownload(pendingAction.getUri(), pendingAction.getType(),
                                pendingAction.getCourse(), pendingAction.getSection(), pendingAction.getItem());
                        break;
                    case DELETE:
                        deleteDownload(pendingAction.getType(),
                                pendingAction.getCourse(), pendingAction.getSection(), pendingAction.getItem());
                        break;
                    case CANCEL:
                        cancelDownload(pendingAction.getType(),
                                pendingAction.getCourse(), pendingAction.getSection(), pendingAction.getItem());
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

    public Download getDownload(DownloadFileType type, Course course, Section section, Item item) {
        Uri downloadUri = buildDownloadUri(type, course, section, item);
        Download dl = new Download();
        dl.localUri = downloadUri.toString();

        if (Config.DEBUG) {
            Log.d(TAG, "Get download " + downloadUri.toString());
        }

        Set<Download> dlSet = DownloadHelper.getAllDownloads();
        for (Download download : dlSet) {
            if (download.equals(dl)) {
                return download;
            }
        }

        return null;
    }

    public boolean downloadRunning(DownloadFileType type, Course course, Section section, Item item) {
        Uri downloadUri = buildDownloadUri(type, course, section, item);
        Download dl = new Download();
        dl.localUri = downloadUri.toString();

        int flags = android.app.DownloadManager.STATUS_PAUSED | android.app.DownloadManager.STATUS_PENDING | android.app.DownloadManager.STATUS_RUNNING;
        Set<Download> dlSet = DownloadHelper.getAllDownloadsForStatus(flags);

        return dlSet.contains(dl);
    }

    public boolean downloadExists(DownloadFileType type, Course course, Section section, Item item) {
        File file = new File(buildDownloadUri(type, course, section, item).getPath());
        return file.isFile() && file.exists();
    }

    private boolean downloadExists(Uri downloadUri) {
        File file = new File(downloadUri.getPath());
        return file.isFile() && file.exists();
    }

    public File getDownloadFile(DownloadFileType type, Course course, Section section, Item item) {
        File file = new File(buildDownloadUri(type, course, section, item).getPath());
        if (file.isFile() && file.exists()) {
            return file;
        }
        return null;
    }

    public long getDownloadFileSize(DownloadFileType type, Course course, Section section, Item item) {
        File file = new File(buildDownloadUri(type, course, section, item).getPath());
        if (file.isFile() && file.exists()) {
            return file.length();
        }
        return 0;
    }

    private Uri buildDownloadUri(DownloadFileType type, Course course, Section section, Item item) {
        File publicAppFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + GlobalApplication.getInstance().getString(R.string.app_name));

        String file = this.escapeFilename(item.title) + type.getFileSuffix();

        return Uri.fromFile(new File(publicAppFolder.getAbsolutePath() + File.separator
                + escapeFilename(course.title) + "_" + course.id + File.separator
                + escapeFilename(section.title) + "_" + section.id + File.separator
                + escapeFilename(item.title) + "_" + item.id + File.separator
                + file));
    }

    private String escapeFilename(String filename) {
        return replaceUmlaute(filename).replaceAll("[^a-zA-Z0-9\\(\\).-]", "_");
    }

    /**
     * Source http://gordon.koefner.at/blog/coding/replacing-german-umlauts/
     */
    private String replaceUmlaute(String input) {
        //replace all lower Umlauts
        String output = input.replace("ü", "ue")
                .replace("ö", "oe")
                .replace("ä", "ae")
                .replace("ß", "ss");

        //first replace all capital umlaute in a non-capitalized context (e.g. Übung)
        output = output.replace("Ü(?=[a-zäöüß ])", "Ue")
                .replace("Ö(?=[a-zäöüß ])", "Oe")
                .replace("Ä(?=[a-zäöüß ])", "Ae");

        //now replace all the other capital umlaute
        output = output.replace("Ü", "UE")
                .replace("Ö", "OE")
                .replace("Ä", "AE");

        return output;
    }

    public List<String> getFoldersWithDownloads() {
        List<String> folders = new ArrayList<>();

        File publicAppFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + GlobalApplication.getInstance().getString(R.string.app_name));

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

    public String getAppFolder() {
        File appFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + GlobalApplication.getInstance().getString(R.string.app_name));

        createFolderIfNotExists(appFolder);

        return appFolder.getAbsolutePath();
    }

    private void createFolderIfNotExists(File file) {
        if (!file.exists()) {
            if (file.isFile()) {
                file = file.getParentFile();
            }

            Log.d(TAG, "Folder " + file.getAbsolutePath() + " not exists");
            if (file.mkdirs()) {
                Log.d(TAG, "Created Folder " + file.getAbsolutePath());
            } else {
                Log.w(TAG, "Failed creating Folder " + file.getAbsolutePath());
            }
        } else {
            Log.d(TAG, "Folder " + file.getAbsolutePath() + " already exists");
        }
    }

    public enum DownloadFileType {
        SLIDES, TRANSCRIPT, VIDEO_SD, VIDEO_HD;

        public static DownloadFileType getDownloadFileTypeFromUri(String uri) {
            if (uri.endsWith(DownloadFileType.SLIDES.getFileSuffix())) {
                return DownloadFileType.SLIDES;
            } else if (uri.endsWith(DownloadFileType.TRANSCRIPT.getFileSuffix())) {
                return DownloadFileType.TRANSCRIPT;
            } else if (uri.endsWith(DownloadFileType.VIDEO_SD.getFileSuffix())) {
                return DownloadFileType.VIDEO_SD;
            } else if (uri.endsWith(DownloadFileType.VIDEO_HD.getFileSuffix())) {
                return DownloadFileType.VIDEO_HD;
            }
            return null;
        }

        public String getFileSuffix() {
            switch (this) {
                case SLIDES:
                    return "_slides.pdf";
                case TRANSCRIPT:
                    return "_transcript.pdf";
                case VIDEO_SD:
                    return "_video_sd.mp4";
                case VIDEO_HD:
                    return "_video_hd.mp4";
                default:
                    return "";
            }
        }

        @Override
        public String toString() {
            switch (this) {
                case SLIDES:
                    return "Slides";
                case TRANSCRIPT:
                    return "Transcript";
                case VIDEO_SD:
                    return "SD Video";
                case VIDEO_HD:
                    return "HD Video";
                default:
                    return "";
            }
        }

    }

}
