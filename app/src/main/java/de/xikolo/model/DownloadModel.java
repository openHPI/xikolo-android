package de.xikolo.model;

import android.os.Environment;
import android.util.Log;

import java.io.File;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;
import de.xikolo.data.net.DownloadHelper;
import de.xikolo.util.ExternalStorageUtil;

public class DownloadModel {
    
    public enum DownloadFileType {
        SLIDES, TRANSCRIPT, VIDEO_SD, VIDEO_HD
    }
    
    public static final String TAG = DownloadModel.class.getSimpleName();
    
    public void startDownload(String uri, DownloadFileType type, Course course, Module module, Item item) {
        Log.d(TAG, "Start download for " + uri);
        if (ExternalStorageUtil.isExternalStorageWritable()) {
            File publicAppFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator 
                    + GlobalApplication.getInstance().getString(R.string.app_name));

            String fileName = item.title;
            
            switch (type) {
                case SLIDES:
                    fileName += "_slides.pdf";
                    break;
                case TRANSCRIPT:
                    fileName += "_transcript.pdf";
                    break;
                case VIDEO_SD:
                    fileName += "_video_sd.pdf";
                    break;
                case VIDEO_HD:
                    fileName += "_video_hd.pdf";
                    break;
            }
            
            File file = new File(publicAppFolder.getAbsolutePath() + File.separator 
                    + course.name + "_" + course.id + File.separator
                    + module.name + "_" + module.id + File.separator
                    + item.title + "_" + item.id + File.separator
                    + fileName);

            createFolderIfNotExists(new File(file.getAbsolutePath().replace(fileName, "")));
            
            DownloadHelper.request(uri, "file://" + file.getAbsolutePath(), fileName);
        } else {
            Log.w(TAG, "No write access for external storage");
        }
        
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
