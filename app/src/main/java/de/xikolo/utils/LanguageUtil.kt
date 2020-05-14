package de.xikolo.utils

import java.util.Locale

object LanguageUtil {
    @OptIn(ExperimentalStdlibApi::class)
    fun toNativeName(tag: String): String {
        val languageTag =
            if (tag == "cn") { // work-around for bug in the backend
                "zh"
            } else {
                tag
            }

        val locale = Locale.forLanguageTag(languageTag)
        return locale.getDisplayLanguage(locale).capitalize(locale)
    }
}
