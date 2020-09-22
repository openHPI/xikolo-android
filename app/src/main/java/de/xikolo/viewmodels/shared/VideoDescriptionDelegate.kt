package de.xikolo.viewmodels.shared

import de.xikolo.models.VideoSubtitles
import de.xikolo.utils.LanguageUtil
import io.realm.RealmList

object VideoDescriptionDelegate {

    fun getAvailableSubtitlesText(
        subtitleTitleString: String,
        subtitleList: RealmList<VideoSubtitles>
    ): String {
        val text = StringBuilder("$subtitleTitleString: ")
        for (subtitles in subtitleList) {
            text.append(LanguageUtil.toLocaleName(subtitles.language)).append(", ")
        }
        return text.delete(text.length - 2, text.length).toString()
    }

    fun isVideoSummaryAvailable(summary: String?): Boolean {
        return summary != null && summary.trim { it <= ' ' }
            .isNotEmpty() && !summary.trim { it <= ' ' }.contentEquals("Enter content")
    }
}
