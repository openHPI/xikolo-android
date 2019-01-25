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
         * Timeout for the app to launch and surpass the splash screen.
         */
        const val WAIT_STARTUP = 10000L

        /**
         * Timeout for UI interactions such as gestures or clicks which only imply an animation or short blocking of interactions.
         */
        const val WAIT_UI_INTERACTION = 1000L

        /**
         * Timeout for short network operations such as loading a single course.
         */
        const val WAIT_LOADING_SHORT = 3000L

        /**
         * Timeout for long network operations such as loading a course list.
         */
        const val WAIT_LOADING_LONG = 7000L

        /**
         * Waits for the app to surpass the splash screen and land on the main view.
         */
        fun waitForAppStartup() {
            Thread.sleep(WAIT_STARTUP)
        }

        /**
         * Opens the navigation drawer via the hamburger menu.
         */
        fun openNavigation() {
            val navButton = onView(
                allOf(
                    withContentDescription(App.getInstance().getString(R.string.navigation_drawer_open)),
                    isDisplayed()
                )
            )

            navButton.perform(ViewActions.click())
        }

        /**
         * Opens the overflow menu.
         */
        fun openOverflowMenu() {
            openActionBarOverflowOrOptionsMenu(App.getInstance())
        }
    }

}
