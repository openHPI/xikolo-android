package de.xikolo


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import de.xikolo.controllers.login.LoginActivity
import de.xikolo.managers.UserManager
import de.xikolo.util.NavigationHelper.Companion.WAIT_LOADING_LONG
import junit.framework.Assert.assertTrue
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(LoginActivity::class.java)

    @Before
    @After
    fun logout() {
        if (UserManager.isAuthorized) {
            UserManager.logout()
        }
    }

    @Test
    fun loginTest() {
        val emailView = onView(
            allOf(withId(R.id.editEmail),
                isDisplayed()))

        emailView.perform(clearText())
        emailView.perform(typeText("ben-noah.engelhaupt@student.hpi.de")) // TODO credentials

        val passwordView = onView(
            allOf(withId(R.id.editPassword),
                isDisplayed()))

        passwordView.perform(clearText())
        passwordView.perform(typeText("youaintgettingmypassword"))

        val loginButton = onView(
            allOf(withId(R.id.btnLogin),
                isDisplayed()))

        loginButton.perform(click())
        Thread.sleep(WAIT_LOADING_LONG)

        assertTrue(UserManager.isAuthorized)
    }
}
