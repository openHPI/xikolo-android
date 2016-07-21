package de.xikolo.util;

import android.os.Build;

import de.xikolo.GlobalApplication;

public class StatusBarUtil {

    public static int getHeight() {
        int result = 0;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return result;
        }

        GlobalApplication application = GlobalApplication.getInstance();

        int resourceId = application.getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (resourceId > 0) {
            result = application.getResources().getDimensionPixelSize(resourceId);
        }

        return result;
    }

}
