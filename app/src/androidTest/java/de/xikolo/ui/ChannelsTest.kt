package de.xikolo.ui


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.FeatureToggle
import de.xikolo.controllers.main.MainActivity
import de.xikolo.mocking.base.BaseMockedTest
import de.xikolo.ui.helper.NavigationHelper
import de.xikolo.ui.helper.NavigationHelper.Companion.WAIT_LOADING_SHORT
import de.xikolo.ui.helper.NavigationHelper.Companion.WAIT_UI_INTERACTION
import de.xikolo.ui.helper.ViewHierarchyHelper.Companion.childAtPosition
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class ChannelsTest : BaseMockedTest() {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    /**
     * Navigates in the app to the channel list. (if applicable)
     */
    @Before
    fun navigateToChannelList() {
        if (!FeatureToggle.channels()) {
            return
        }

        NavigationHelper.waitForAppStartup()

        NavigationHelper.openNavigation()
        Thread.sleep(WAIT_UI_INTERACTION)

        val channelsButton = onView(
            withText(App.getInstance().getString(R.string.title_section_channels))
        )

        channelsButton.perform(click())
        Thread.sleep(WAIT_LOADING_SHORT)
    }

    /**
     * Tests the loading behavior of the channels list. (if applicable)
     * This is done by testing refreshing via the overflow menu and then asserting at least one list item is shown.
     */
    @Test
    fun channelLoading() {
        if (!FeatureToggle.channels()) {
            return
        }

        NavigationHelper.openOverflowMenu()

        val refreshView = onView(
            allOf(
                withText(App.getInstance().getString(R.string.action_refresh)),
                isDisplayed()
            )
        )

        refreshView.perform(click())
        Thread.sleep(WAIT_LOADING_SHORT)

        val cardView = onView(
            allOf(
                withId(R.id.container),
                childAtPosition(
                    allOf(
                        withId(R.id.content_view),
                        childAtPosition(
                            withId(R.id.refresh_layout),
                            0
                        )
                    ),
                    0
                )
            )
        )

        cardView.check(
            ViewAssertions.matches(isDisplayed())
        )
    }

    /**
     * Tests the behavior of the channel details view. (if applicable)
     * The first item in the channel list is clicked and then a swipe gesture is performed to simulate scrolling.
     */
    @Test
    fun channelDetails() {
        if (!FeatureToggle.channels()) {
            return
        }

        val cardView = onView(
            allOf(
                withId(R.id.container),
                childAtPosition(
                    allOf(
                        withId(R.id.content_view),
                        childAtPosition(
                            withId(R.id.refresh_layout),
                            0
                        )
                    ),
                    0
                )
            )
        )

        cardView.check(
            ViewAssertions.matches(isDisplayed())
        )

        cardView.perform(click())
        Thread.sleep(WAIT_UI_INTERACTION)

        onView(
            withId(R.id.contentLayout)
        ).perform(swipeUp())
        Thread.sleep(WAIT_UI_INTERACTION)

        pressBack()
    }

}
