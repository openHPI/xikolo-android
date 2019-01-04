package de.xikolo.test


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import de.xikolo.R
import de.xikolo.controllers.main.MainActivity
import de.xikolo.test.util.AssertionHelper
import de.xikolo.test.util.NavigationHelper
import de.xikolo.test.util.NavigationHelper.Companion.WAIT_UI_INTERACTION
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun waitForAppStartup() {
        NavigationHelper.waitForAppStartup()
    }

    @Test
    fun navigationTest() {
        NavigationHelper.openNavigation()
        Thread.sleep(WAIT_UI_INTERACTION)

        pressBack()
        Thread.sleep(WAIT_UI_INTERACTION)

        NavigationHelper.openNavigation()
        Thread.sleep(WAIT_UI_INTERACTION)

        val drawerLayout = onView(
            allOf(withId(R.id.drawer_layout),
                isDisplayed()))

        drawerLayout.perform(swipeLeft())
        Thread.sleep(WAIT_UI_INTERACTION)

        AssertionHelper.assertMainShown()
    }
}
