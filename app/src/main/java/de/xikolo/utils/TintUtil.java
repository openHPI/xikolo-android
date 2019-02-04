package de.xikolo.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import de.xikolo.BuildConfig;
import de.xikolo.R;
import de.xikolo.config.BuildFlavor;

public class TintUtil {

    public static void tintMenu(Context context, Menu menu) {
        // tint menu icons dark on mooc.house, cause toolbar has light background
        if (BuildConfig.X_FLAVOR == BuildFlavor.MOOC_HOUSE) {
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (item.getIcon() != null && item.getItemId() != R.id.media_route_menu_item) {
                    TintUtil.tintMenuIconDark(context, item);
                }
            }
        }
    }

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
