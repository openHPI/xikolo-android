package de.xikolo.ui


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import de.xikolo.R
import de.xikolo.controllers.main.MainActivity
import de.xikolo.mocking.base.BaseMockedTest
import de.xikolo.ui.helper.AssertionHelper
import de.xikolo.ui.helper.NavigationHelper
import de.xikolo.ui.helper.NavigationHelper.Companion.WAIT_UI_INTERACTION
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class NavigationTest : BaseMockedTest() {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun waitForAppStartup() {
        NavigationHelper.waitForAppStartup()
    }

    /**
     * Tests the behavior of the navigation drawer.
     * It consists of opening and closing via the hamburger menu and via swipe gestures.
     */
    @Test
    fun navigationTest() {
        NavigationHelper.openNavigation()
        Thread.sleep(WAIT_UI_INTERACTION)

        pressBack()
        Thread.sleep(WAIT_UI_INTERACTION)

        NavigationHelper.openNavigation()
        Thread.sleep(WAIT_UI_INTERACTION)

        val drawerLayout = onView(
            allOf(
                withId(R.id.drawer_layout),
                isDisplayed()
            )
        )

        drawerLayout.perform(swipeLeft())
        Thread.sleep(WAIT_UI_INTERACTION)

        AssertionHelper.assertMainShown()
    }

}
