package de.xikolo.test


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import de.xikolo.App
import de.xikolo.R
import de.xikolo.controllers.main.MainActivity
import de.xikolo.test.util.NavigationHelper
import de.xikolo.test.util.NavigationHelper.Companion.WAIT_LOADING_SHORT
import de.xikolo.test.util.NavigationHelper.Companion.WAIT_UI_INTERACTION
import de.xikolo.test.util.ViewHierarchyHelper.Companion.childAtPosition
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class ChannelsTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun navigateToChannelList() {
        NavigationHelper.waitForAppStartup()

        NavigationHelper.openNavigation()
        Thread.sleep(WAIT_UI_INTERACTION)

        val channelsButton = onView(
            withText(App.getInstance().getString(R.string.title_section_channels))
        )

        channelsButton.perform(click())
        Thread.sleep(WAIT_LOADING_SHORT)
    }

    @Test
    fun channelLoading() {
        NavigationHelper.openOverflowMenu()

        val refreshView = onView(
            allOf(withText(App.getInstance().getString(R.string.action_refresh)),
                isDisplayed()))

        refreshView.perform(click())
        Thread.sleep(WAIT_LOADING_SHORT)

        val cardView = onView(
            allOf(withId(R.id.container),
                childAtPosition(
                    allOf(withId(R.id.content_view),
                        childAtPosition(
                            withId(R.id.refresh_layout),
                            0)),
                    0)))

        cardView.check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun channelDetails() {
        val cardView = onView(
            allOf(withId(R.id.container),
                childAtPosition(
                    allOf(withId(R.id.content_view),
                        childAtPosition(
                            withId(R.id.refresh_layout),
                            0)),
                    0)))

        cardView.check(ViewAssertions.matches(isDisplayed()))

        cardView.perform(click())
        Thread.sleep(WAIT_UI_INTERACTION)

        onView(
            withId(R.id.contentLayout)
        ).perform(swipeUp())
        Thread.sleep(WAIT_UI_INTERACTION)

        pressBack()
    }

}
