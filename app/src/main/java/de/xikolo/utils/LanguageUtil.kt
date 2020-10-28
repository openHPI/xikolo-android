package de.xikolo.utils

import java.util.Locale

object LanguageUtil {
    var deviceLanguage: String = Locale.getDefault().language.takeIf { it != "zh" } ?: "cn"

    fun toNativeName(tag: String): String {
        val languageTag = correctLanguageTag(tag)

        val locale = Locale.forLanguageTag(languageTag)
        return locale.getDisplayLanguage(locale).capitalize(locale)
    }

    fun toLocaleName(tag: String): String {
        val languageTag = correctLanguageTag(tag)
        val locale = Locale.forLanguageTag(languageTag)
        val deviceLocale = Locale.getDefault()

        return locale.getDisplayLanguage(deviceLocale).capitalize(locale)
    }

    private fun correctLanguageTag(tag: String): String {
        // work-around for wrong chinese API locale in backend
        return if (tag == "cn") {
            "zh"
        } else {
            tag
        }
    }
}
