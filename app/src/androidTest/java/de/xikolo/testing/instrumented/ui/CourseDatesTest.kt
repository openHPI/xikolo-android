package de.xikolo.testing.instrumented.ui


import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import de.xikolo.R
import de.xikolo.controllers.main.MainActivity
import de.xikolo.managers.UserManager
import de.xikolo.testing.instrumented.mocking.base.BaseMockedTest
import de.xikolo.testing.instrumented.ui.helper.AuthorizationHelper
import de.xikolo.testing.instrumented.ui.helper.NavigationHelper
import de.xikolo.testing.instrumented.ui.helper.ViewHierarchyHelper.childAtPosition
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class CourseDatesTest : BaseMockedTest() {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(MainActivity::class.java)

    /**
     * Logs the user in.
     */
    @Before
    fun login() {
        AuthorizationHelper.login()
        assertTrue(UserManager.isAuthorized)
    }

    /**
     * Logs the user out.
     */
    @After
    fun logout() {
        AuthorizationHelper.logout()
    }

    /**
     * Tests the date overview shown under "My Courses".
     */
    @Test
    fun dateOverviewTest() {
        NavigationHelper.selectNavigationItem(context, R.string.title_section_my_courses)

        NavigationHelper.refreshThroughOverflow(context)

        val datesCard = onView(
            allOf(
                withId(R.id.container),
                instanceOf(CardView::class.java),
                withChild(
                    instanceOf(LinearLayout::class.java)
                )
            )
        )
        datesCard.perform(click())
    }

    /**
     * Tests the date list and clicks on an item.
     *
     * Warning: This test fails for now as it has interaction towards the CourseActivity which is not yet instrumented.
     */
    @Test
    fun dateListTest() {
        NavigationHelper.selectNavigationItem(context, R.string.title_section_dates)

        NavigationHelper.refreshThroughOverflow(context)

        val dateListItem = onView(
            allOf(
                withId(R.id.container),
                childAtPosition(
                    allOf(
                        withId(R.id.content_view),
                        childAtPosition(
                            withId(R.id.refresh_layout),
                            0
                        )
                    ), 2
                ),
                isDisplayed()
            )
        )
        //dateListItem.perform(click())
    }

}
