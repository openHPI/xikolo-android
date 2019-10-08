@file:JvmName("StyleExtensions")

package de.xikolo.utils.extensions

import android.content.Context
import android.view.Menu
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.BuildFlavor

fun <T : Context> T.tintMenu(menu: Menu) {
    // tint menu icons dark on mooc.house, cause toolbar has light background
    if (BuildConfig.X_FLAVOR === BuildFlavor.MOOC_HOUSE) {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            if (item.icon != null && item.itemId != R.id.media_route_menu_item) {
                val normalDrawable = item.icon
                val wrapDrawable = DrawableCompat.wrap(normalDrawable)
                DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(this, R.color.text_main))

                item.icon = wrapDrawable
            }
        }
    }
}
