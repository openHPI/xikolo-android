package de.xikolo.controllers.helper

import androidx.annotation.StringRes
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
    CERTIFICATES(R.string.tab_certificates),
    DOCUMENTS(R.string.tab_documents),
    ANNOUNCEMENTS(R.string.tab_announcements),
    RECAP(R.string.tab_recap);

    abstract class State {

        protected val areas: MutableList<CourseArea> = mutableListOf()

        val size: Int
            get() = areas.size

        fun get(index: Int): CourseArea = areas[index]

        fun indexOf(area: CourseArea): Int = areas.indexOf(area)

    }

    object All : State() {
        init {
            areas.add(CourseArea.LEARNINGS)
            areas.add(CourseArea.DISCUSSIONS)
            areas.add(CourseArea.PROGRESS)
            areas.add(CourseArea.COURSE_DETAILS)
            areas.add(CourseArea.CERTIFICATES)
            if (FeatureToggle.documents()) areas.add(CourseArea.DOCUMENTS)
            areas.add(CourseArea.ANNOUNCEMENTS)
            if (FeatureToggle.recapMode()) areas.add(CourseArea.RECAP)
        }
    }

    object Locked : State() {
        init {
            areas.add(CourseArea.COURSE_DETAILS)
            areas.add(CourseArea.CERTIFICATES)
        }
    }

}
