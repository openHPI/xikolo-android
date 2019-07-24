package de.xikolo.testing.instrumented.ui.helper

import android.content.Context
import androidx.annotation.IntegerRes
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import de.xikolo.R
import org.hamcrest.Matchers.allOf

object NavigationHelper {

    /**
     * Timeout for very short UI events.
     */
    const val WAIT_UI_SHORT = 50L

    /**
     * Timeout for short UI events such as animations.
     */
    const val WAIT_UI_ANIMATION = 300L

    /**
     * Timeout for longer UI events.
     */
    const val WAIT_UI_LONG = 1000L

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
    fun openNavigation(context: Context) {
        val navButton = onView(
            allOf(
                withContentDescription(context.getString(R.string.navigation_drawer_open)),
                isDisplayed()
            )
        )

        navButton.perform(ViewActions.click())
        Thread.sleep(WAIT_UI_ANIMATION)
    }

    /**
     * Opens the navigation and selects the navigation item based on its text.
     */
    fun selectNavigationItem(context: Context, @IntegerRes withText: Int) {
        openNavigation(context)

        val itemButton = onView(
            ViewMatchers.withText(context.getString(withText))
        )

        itemButton.perform(ViewActions.click())
        Thread.sleep(WAIT_UI_ANIMATION)
    }

    /**
     * Opens the overflow menu.
     */
    fun openOverflowMenu(context: Context) {
        openActionBarOverflowOrOptionsMenu(context)
        Thread.sleep(WAIT_UI_SHORT)
    }

    /**
     * Clicks the refresh menu item in the overflow menu.
     */
    fun refreshThroughOverflow(context: Context) {
        openOverflowMenu(context)

        val refreshView = onView(
            allOf(
                ViewMatchers.withText(context.getString(R.string.action_refresh)),
                isDisplayed()
            )
        )

        refreshView.perform(ViewActions.click())
        Thread.sleep(WAIT_UI_SHORT)
    }
}
