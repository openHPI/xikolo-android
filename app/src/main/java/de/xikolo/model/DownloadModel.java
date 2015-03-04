package de.xikolo.model;

import android.app.DownloadManager;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Set;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Download;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.net.DownloadHelper;
import de.xikolo.util.Config;
import de.xikolo.util.ExternalStorageUtil;
import de.xikolo.util.ToastUtil;

public class DownloadModel {
    
    public enum DownloadFileType {
        SLIDES, TRANSCRIPT, VIDEO_SD, VIDEO_HD;
        
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
        
    }
    
    public static final String TAG = DownloadModel.class.getSimpleName();
    
    public void startDownload(String uri, DownloadFileType type, Course course, Module module, Item item) {
        if (Config.DEBUG) {
            Log.d(TAG, "Start download for " + uri);
        }
        if (ExternalStorageUtil.isExternalStorageWritable()) {
            String file = item.title + type.getFileSuffix();
            String filename = buildDownloadFilename(type, course, module, item);
            
            if (downloadExists(filename)) {
                ToastUtil.show(GlobalApplication.getInstance(), R.string.notification_file_already_downloaded);
            } else {

                File dlFile = new File(filename);

                createFolderIfNotExists(new File(dlFile.getAbsolutePath().replace(file, "")));

                DownloadHelper.request(uri, "file://" + dlFile.getAbsolutePath(), file);
            }
        } else {
            Log.w(TAG, "No write access for external storage");
            ToastUtil.show(GlobalApplication.getInstance(), R.string.notification_no_external_write_access);
        }
    }

    public boolean downloadRunning(DownloadFileType type, Course course, Module module, Item item) {
        String dlFilename = buildDownloadFilename(type, course, module, item);
        Download dl = new Download();
        dl.localFilename = dlFilename;
        
        int flags = DownloadManager.STATUS_PAUSED | DownloadManager.STATUS_PENDING | DownloadManager.STATUS_RUNNING;
        Set<Download> dlSet = DownloadHelper.getAllDownloadsForStatus(flags);
        
        return dlSet.contains(dl);
    }
    
    public boolean downloadExists(DownloadFileType type, Course course, Module module, Item item) {
        File file = new File(buildDownloadFilename(type, course, module, item));
        return file.isFile() && file.exists();
    }
    
    private boolean downloadExists(String filename) {
        File file = new File(filename);
        return file.isFile() && file.exists();
    }
    
    private String buildDownloadFilename(DownloadFileType type, Course course, Module module, Item item) {
        File publicAppFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + GlobalApplication.getInstance().getString(R.string.app_name));
        
        String file = item.title + type.getFileSuffix();

        return publicAppFolder.getAbsolutePath() + File.separator
                + course.name + "_" + course.id + File.separator
                + module.name + "_" + module.id + File.separator
                + item.title + "_" + item.id + File.separator
                + file;
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
    
}
