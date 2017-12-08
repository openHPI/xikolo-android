package de.xikolo.utils;

import android.support.annotation.StringRes;
import android.widget.Toast;

import de.xikolo.App;

public class ToastUtil {

    public static void show(String message) {
        Toast.makeText(App.getInstance(), message, Toast.LENGTH_SHORT).show();
    }

    public static void show(@StringRes int stringId) {
        Toast.makeText(App.getInstance(), App.getInstance().getString(stringId), Toast.LENGTH_SHORT).show();
    }

    public static void show(String message, int gravity, int xOffset, int yOffset) {
        Toast toast = Toast.makeText(App.getInstance(), message, Toast.LENGTH_SHORT);
        toast.setGravity(gravity, xOffset, yOffset);
        toast.show();
    }

    public static void show(@StringRes int stringId, int gravity, int xOffset, int yOffset) {
        Toast toast = Toast.makeText(App.getInstance(), App.getInstance().getString(stringId), Toast.LENGTH_SHORT);
        toast.setGravity(gravity, xOffset, yOffset);
        toast.show();
    }

}
