package de.xikolo.util

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

        const val WAIT_STARTUP = 10000L
        const val WAIT_UI_INTERACTION = 1000L
        const val WAIT_LOADING_SHORT = 3000L
        const val WAIT_LOADING_LONG = 7000L

        fun waitForAppStartup() {
            Thread.sleep(WAIT_STARTUP)
        }

        fun openNavigation() {
            val navButton = onView(
                allOf(withContentDescription(App.getInstance().getString(R.string.navigation_drawer_open)),
                    isDisplayed()))

            navButton.perform(ViewActions.click())
        }

        fun openOverflowMenu() {
            openActionBarOverflowOrOptionsMenu(App.getInstance())
        }
    }

}
