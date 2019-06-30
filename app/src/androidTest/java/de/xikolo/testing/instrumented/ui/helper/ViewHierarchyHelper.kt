package de.xikolo.testing.instrumented.ui.helper

import android.view.View
import android.view.ViewGroup
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

class ViewHierarchyHelper {

    companion object {

        /**
         * Matches a child at a specific position in its parents view hierarchy.
         */
        fun childAtPosition(parentMatcher: Matcher<View>, position: Int): Matcher<View> {
            return object : TypeSafeMatcher<View>() {

                override fun describeTo(description: Description) {
                    description.appendText("Child at position $position in parent ")
                    parentMatcher.describeTo(description)
                }

                public override fun matchesSafely(view: View): Boolean {
                    val parent = view.parent
                    return parent is ViewGroup
                        && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
                }
            }
        }
    }

}
