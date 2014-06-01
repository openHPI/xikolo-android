package de.xikolo.util;

import android.content.Context;
import android.widget.Toast;

public class Toaster {

    public static void show(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
