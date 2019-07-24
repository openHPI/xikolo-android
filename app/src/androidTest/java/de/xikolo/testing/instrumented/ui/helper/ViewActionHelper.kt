package de.xikolo.testing.instrumented.ui.helper

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Matcher

object ViewActionHelper {

    fun clickIgnoringConstraints(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return ViewMatchers.isEnabled()
            }

            override fun getDescription(): String {
                return "click button"
            }

            override fun perform(uiController: UiController?, view: View?) {
                view?.performClick()
            }
        }
    }
}