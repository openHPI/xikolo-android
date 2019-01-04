package de.xikolo.test.util

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import de.xikolo.App
import de.xikolo.R
import org.hamcrest.Matchers.allOf
import org.junit.Test

class AssertionHelper {

    companion object {

        // check for 'All courses' text in status bar
        @Test
        fun assertMainShown() {
            val toolbarTitle = onView(
                allOf(withText(App.getInstance().getString(R.string.title_section_all_courses)),
                    //isDescendantOfA(isA(Toolbar::class.java)),
                    isDisplayed()))

            toolbarTitle.check(ViewAssertions.matches(isDisplayed()))
        }
    }

}
