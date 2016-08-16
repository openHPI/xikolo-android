package de.xikolo.util;

import android.content.res.TypedArray;
import android.os.Build;

import de.xikolo.GlobalApplication;

public class AndroidDimenUtil {

    public static int getStatusBarHeight() {
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

    public static int getActionBarHeight() {
        int result = 0;

        GlobalApplication application = GlobalApplication.getInstance();

        final TypedArray styledAttributes = application.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        result = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return result;
    }

}
