package de.xikolo.util;

import android.content.Context;
import android.text.format.Formatter;

import java.io.File;

/**
 * @author Denis Fyedyayev, 6/12/15.
 */
public class StringUtil {
    public static String getUsableMemory(Context context, File file) {
        return Formatter.formatFileSize(context, file.getUsableSpace());
    }
}
