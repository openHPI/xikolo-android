package de.xikolo.testing.instrumented.ui


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.swipeUp
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import de.xikolo.R
import de.xikolo.config.Feature
import de.xikolo.controllers.main.MainActivity
import de.xikolo.testing.instrumented.mocking.base.BaseMockedTest
import de.xikolo.testing.instrumented.ui.helper.NavigationHelper
import de.xikolo.testing.instrumented.ui.helper.ViewHierarchyHelper.childAtPosition
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class ChannelsTest : BaseMockedTest() {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(MainActivity::class.java)

    /**
     * Navigates in the app to the channel list. (if applicable)
     */
    @Before
    fun navigateToChannelList() {
        if (!Feature.enabled("channels")) {
            return
        }

        NavigationHelper.selectNavigationItem(context, R.string.title_section_channels)
    }

    /**
     * Tests the loading behavior of the channels list. (if applicable)
     * This is done by testing refreshing via the overflow menu and then asserting at least one list item is shown.
     */
    @Test
    fun channelLoading() {
        if (!Feature.enabled("channels")) {
            return
        }

        NavigationHelper.refreshThroughOverflow(context)

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
        if (!Feature.enabled("channels")) {
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

        onView(
            withId(R.id.contentLayout)
        ).perform(swipeUp())

        pressBack()
    }

}
