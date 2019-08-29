package de.xikolo.controllers.helper

import androidx.annotation.StringRes
import de.xikolo.R
import de.xikolo.config.FeatureConfig

enum class CourseListFilter {
    ALL, MY
}

enum class CourseArea(@StringRes val titleRes: Int) {

    LEARNINGS(R.string.tab_learnings),
    DISCUSSIONS(R.string.tab_discussions),
    PROGRESS(R.string.tab_progress),
    COURSE_DETAILS(R.string.tab_course_details),
    CERTIFICATES(R.string.tab_certificates),
    DOCUMENTS(R.string.tab_documents),
    ANNOUNCEMENTS(R.string.tab_announcements),
    RECAP(R.string.tab_recap);

    abstract class State {

        val areas: MutableList<CourseArea> = mutableListOf()

        val size: Int
            get() = areas.size

        fun get(index: Int): CourseArea = areas[index]

        fun indexOf(area: CourseArea): Int = areas.indexOf(area)

    }

    object All : State() {
        init {
            areas.add(LEARNINGS)
            areas.add(DISCUSSIONS)
            areas.add(PROGRESS)
            areas.add(COURSE_DETAILS)
            areas.add(CERTIFICATES)
            if (FeatureConfig.DOCUMENTS) areas.add(DOCUMENTS)
            areas.add(ANNOUNCEMENTS)
            if (FeatureConfig.RECAP_MODE) areas.add(RECAP)
        }
    }

    object Locked : State() {
        init {
            areas.add(COURSE_DETAILS)
            areas.add(CERTIFICATES)
        }
    }

    object External : State() {
        init {
            areas.add(COURSE_DETAILS)
        }
    }

}
