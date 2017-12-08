package de.xikolo.utils;

import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

import de.xikolo.App;

public class FileProviderUtil extends FileProvider {

    public static Uri getUriForFile(File file) {
        return getUriForFile(App.getInstance(), "de.xikolo.fileprovider", file);
    }

}
