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
import de.xikolo.controllers.main.MainActivity
import de.xikolo.testing.instrumented.mocking.base.BaseMockedTest
import de.xikolo.testing.instrumented.ui.helper.NavigationHelper
import de.xikolo.testing.instrumented.ui.helper.ViewHierarchyHelper.Companion.childAtPosition
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class AllCoursesTest : BaseMockedTest() {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(MainActivity::class.java)

    /**
     * Navigates in the app to the course list.
     */
    @Before
    fun navigateToCourseListAll() {
        NavigationHelper.selectNavigationItem(context, R.string.title_section_all_courses)
    }

    /**
     * Tests the loading behavior of the course list.
     * This is done by testing refreshing via the overflow menu and then asserting at least one list item is shown.
     * Then this list is scrolled.
     */
    @Test
    fun coursesLoading() {
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

        onView(
            withId(R.id.content_view)
        ).perform(swipeUp())
    }

    /**
     * Tests the behavior of the course details view.
     * The first item in the all courses list is clicked.
     */
    @Test
    fun courseDetails() {
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
                    1
                )
            )
        )

        cardView.check(
            ViewAssertions.matches(isDisplayed())
        )

        cardView.perform(click())

        pressBack()
    }

}
