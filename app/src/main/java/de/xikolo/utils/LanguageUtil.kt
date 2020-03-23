package de.xikolo.utils

import androidx.annotation.StringRes
import de.xikolo.App

object LanguageUtil {
    fun toNativeName(code: String): String {
        val context = App.Companion.instance

        @StringRes val resId = context.resources.getIdentifier(
            "lang_$code",
            "string",
            context.packageName
        )

        return if (resId == 0) code else context.getString(resId)
    }
}
