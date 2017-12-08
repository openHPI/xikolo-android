package de.xikolo.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.DecimalFormat;

import de.xikolo.App;
import de.xikolo.R;

public class FileUtil {

    public static final String TAG = FileUtil.class.getSimpleName();

    public static String getFormattedFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static long folderSize(File directory) {
        long length = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                length += file.length();
            } else {
                length += folderSize(file);
            }
        }
        return length;
    }

    public static long folderFileNumber(File directory) {
        long files = 0;
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                files++;
            } else {
                files += folderFileNumber(file);
            }
        }
        return files;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void delete(File file) {
        if (file.isDirectory()) {
            if (file.list().length == 0) {
                file.delete();
            } else {
                String files[] = file.list();
                for (String temp : files) {
                    File fileDelete = new File(file, temp);
                    delete(fileDelete);
                }
                if (file.list().length == 0) {
                    file.delete();
                }
            }
        } else {
            file.delete();
        }
    }

    public static void createFolderIfNotExists(File file) {
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

    public static String createPublicAppFolderPath() {
        File appFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + App.getInstance().getString(R.string.app_name));

        createFolderIfNotExists(appFolder);

        return appFolder.getAbsolutePath();
    }

    public static String escapeFilename(String filename) {
        return replaceUmlaute(filename).replaceAll("[^a-zA-Z0-9\\(\\).-]", "_");
    }

    /**
     * Source http://gordon.koefner.at/blog/coding/replacing-german-umlauts/
     */
    private static String replaceUmlaute(String input) {
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

}
