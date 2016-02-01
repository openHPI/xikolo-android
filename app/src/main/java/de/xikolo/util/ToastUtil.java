package de.xikolo.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    public static void show(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void show(Context context, int stringId) {
        Toast.makeText(context, context.getString(stringId), Toast.LENGTH_SHORT).show();
    }

    public static void show(Context context, String message, int gravity, int xOffset, int yOffset) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(gravity, xOffset, yOffset);
        toast.show();
    }

    public static void show(Context context, int stringId, int gravity, int xOffset, int yOffset) {
        Toast toast = Toast.makeText(context, context.getString(stringId), Toast.LENGTH_SHORT);
        toast.setGravity(gravity, xOffset, yOffset);
        toast.show();
    }

}
