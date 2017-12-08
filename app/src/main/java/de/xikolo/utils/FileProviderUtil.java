package de.xikolo.utils;

import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

import de.xikolo.App;
import de.xikolo.BuildConfig;

public class FileProviderUtil extends FileProvider {

    public static Uri getUriForFile(File file) {
        return getUriForFile(App.getInstance(), BuildConfig.APPLICATION_ID + ".fileprovider", file);
    }

}
