package de.xikolo.ui.helper

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import de.xikolo.App
import de.xikolo.R
import org.hamcrest.Matchers.allOf

class NavigationHelper {

    companion object {

        /**
         * Timeout for short network operations such as loading a single course.
         */
        const val WAIT_LOADING_SHORT = 3000L

        /**
         * Timeout for long network operations such as loading a course list.
         */
        const val WAIT_LOADING_LONG = 7000L

        /**
         * Opens the navigation drawer via the hamburger menu.
         */
        fun openNavigation() {
            val navButton = onView(
                allOf(
                    withContentDescription(App.instance.getString(R.string.navigation_drawer_open)),
                    isDisplayed()
                )
            )

            navButton.perform(ViewActions.click())
        }

        /**
         * Opens the overflow menu.
         */
        fun openOverflowMenu() {
            openActionBarOverflowOrOptionsMenu(App.instance)
        }
    }

}
