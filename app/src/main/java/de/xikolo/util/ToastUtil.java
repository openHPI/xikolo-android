package de.xikolo.util;

import android.support.annotation.StringRes;
import android.widget.Toast;

import de.xikolo.GlobalApplication;

public class ToastUtil {

    public static void show(String message) {
        Toast.makeText(GlobalApplication.getInstance(), message, Toast.LENGTH_SHORT).show();
    }

    public static void show(@StringRes int stringId) {
        Toast.makeText(GlobalApplication.getInstance(), GlobalApplication.getInstance().getString(stringId), Toast.LENGTH_SHORT).show();
    }

    public static void show(String message, int gravity, int xOffset, int yOffset) {
        Toast toast = Toast.makeText(GlobalApplication.getInstance(), message, Toast.LENGTH_SHORT);
        toast.setGravity(gravity, xOffset, yOffset);
        toast.show();
    }

    public static void show(@StringRes int stringId, int gravity, int xOffset, int yOffset) {
        Toast toast = Toast.makeText(GlobalApplication.getInstance(), GlobalApplication.getInstance().getString(stringId), Toast.LENGTH_SHORT);
        toast.setGravity(gravity, xOffset, yOffset);
        toast.show();
    }

}
