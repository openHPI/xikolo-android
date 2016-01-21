package de.xikolo.controller.helper;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

import de.xikolo.GlobalApplication;
import de.xikolo.R;
import de.xikolo.controller.ModuleActivity;
import de.xikolo.data.entities.Course;
import de.xikolo.data.entities.Item;
import de.xikolo.data.entities.Module;

public class CacheController {

    public static final String TAG = CacheController.class.getSimpleName();
    public final String FILENAME;
    private String filename;
    private Context context;
    private File file;
    private Course course;
    private Module module;
    private Item item;

    public CacheController() {
        context = GlobalApplication.getInstance();
        FILENAME = GlobalApplication.getInstance().getResources().getString(R.string.filename_cache_lastcourse);
        filename = context.getCacheDir().getAbsolutePath() + File.separator + FILENAME;
        file = new File(filename);
        createFolderIfNotExists(new File(file.getAbsolutePath().replace(FILENAME, "")));
    }

    public void setCachedExtras(Bundle b) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(b.getParcelable(ModuleActivity.ARG_COURSE));
            oos.writeObject(b.getParcelable(ModuleActivity.ARG_MODULE));
            oos.writeObject(b.getParcelable(ModuleActivity.ARG_ITEM));
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readCachedExtras() {
        Bundle bundle = new Bundle();
        FileInputStream fis;
        ObjectInputStream ois;
        try {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            course = (Course) ois.readObject();
            module = (Module) ois.readObject();
            item = (Item) ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
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

    public Module getModule() {
        return module;
    }
}
