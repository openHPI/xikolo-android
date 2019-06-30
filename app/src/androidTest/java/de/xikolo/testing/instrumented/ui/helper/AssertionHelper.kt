package de.xikolo.testing.instrumented.ui.helper

import android.content.Context
import androidx.appcompat.widget.Toolbar
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import de.xikolo.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.junit.Test

class AssertionHelper {

    companion object {

        /**
         * Asserts that the main view with the list of all courses is shown.
         * This is done by checking whether the 'All Courses' title is shown in the toolbar.
         */
        @Test
        fun assertMainShown(context: Context) {
            val toolbarTitle = onView(
                allOf(
                    withText(context.getString(R.string.title_section_all_courses)),
                    isDescendantOfA(instanceOf(Toolbar::class.java)),
                    isDisplayed()
                )
            )

            toolbarTitle.check(
                ViewAssertions.matches(
                    isDisplayed()
                )
            )
        }
    }

}
