package de.xikolo.controllers.helper;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.models.Course;
import de.xikolo.models.Item;
import de.xikolo.models.Section;

public class CacheHelper {

    public static final String TAG = CacheHelper.class.getSimpleName();

    public final String FILENAME;

    private File file;
    private Course course;
    private Section module;
    private Item item;

    public CacheHelper() {
        Context context = App.getInstance();
        FILENAME = App.getInstance().getResources().getString(R.string.filename_cache_lastcourse);
        String filename = context.getCacheDir().getAbsolutePath() + File.separator + FILENAME;
        file = new File(filename);
        createFolderIfNotExists(new File(file.getAbsolutePath().replace(FILENAME, "")));
    }

    public void setCachedExtras(Bundle b) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(b.getParcelable("course"));
            oos.writeObject(b.getParcelable("section"));
            oos.writeObject(b.getParcelable("item"));
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readCachedExtras() {
        FileInputStream fis;
        ObjectInputStream ois;
        try {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            course = (Course) ois.readObject();
            module = (Section) ois.readObject();
            item = (Item) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
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

    public Course getCourse() {
        return course;
    }

    public Item getItem() {
        return item;
    }

    public Section getModule() {
        return module;
    }
}
