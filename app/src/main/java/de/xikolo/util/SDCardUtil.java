package de.xikolo.util;

import android.os.Environment;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;

/**
 * @author Denis Fyedyayev, 6/14/15.
 */
public class SDCardUtil {
    /**
     * Search for SD Card path.
     * @return path of SD Card or null if it does not exists.
     */
    public static String getSDCardPath() {
        String result = null;
        HashSet<String> externalMounts = getExternalMounts();
        // We do not need the external memory.
        String path = Environment.getExternalStorageDirectory().getPath();

        for (String externalMount : externalMounts) {
            if (!externalMount.equals(path)) {
                result = externalMount;
                break;
            }
        }

        return result;
    }

    /**
     * Search for mounts.
     * @return a set of external memory paths.
     */
    public static HashSet<String> getExternalMounts() {
        final HashSet<String> out = new HashSet<>();
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s = "";
        try {
            final Process process = new ProcessBuilder().command("mount")
                    .redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                s = s + new String(buffer);
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // parse output
        final String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.toLowerCase(Locale.US).contains("asec")) {
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold"))
                                out.add(part);
                    }
                }
            }
        }
        return out;
    }
}
