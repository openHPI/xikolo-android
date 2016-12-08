package de.xikolo.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.MenuItem;

import de.xikolo.R;

public class TintUtil {

    public static void tintMenuIcon(Context context, MenuItem item, @ColorRes int color) {
        Drawable normalDrawable = item.getIcon();
        Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(context, color));

        item.setIcon(wrapDrawable);
    }

    public static void tintMenuIconDark(Context context, MenuItem item) {
        tintMenuIcon(context, item, R.color.text_main);
    }

    public static void tintMenuIconLight(Context context, MenuItem item) {
        tintMenuIcon(context, item, R.color.white);
    }

}
