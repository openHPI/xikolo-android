package de.xikolo.controllers.helper

import android.support.annotation.StringRes
import de.xikolo.R
import de.xikolo.config.FeatureToggle

enum class CourseListFilter {
    ALL, MY
}

enum class CourseArea(@StringRes val titleRes: Int) {

    LEARNINGS(R.string.tab_learnings),
    DISCUSSIONS(R.string.tab_discussions),
    PROGRESS(R.string.tab_progress),
    COURSE_DETAILS(R.string.tab_course_details),
    DOCUMENTS(R.string.tab_documents),
    ANNOUNCEMENTS(R.string.tab_announcements),
    RECAP(R.string.tab_recap);

    val index: Int
        get() = indexOf(this)

    companion object {

        private val areas: MutableList<CourseArea> = mutableListOf()

        init {
            areas.add(LEARNINGS)
            areas.add(DISCUSSIONS)
            areas.add(PROGRESS)
            areas.add(COURSE_DETAILS)
            if (FeatureToggle.documents()) areas.add(DOCUMENTS)
            areas.add(ANNOUNCEMENTS)
            if (FeatureToggle.recapMode()) areas.add(RECAP)
        }

        @JvmStatic
        val size: Int
            get() = areas.size

        @JvmStatic
        fun get(index: Int): CourseArea = areas[index]

        @JvmStatic
        fun indexOf(area: CourseArea): Int = areas.indexOf(area)

    }

}
