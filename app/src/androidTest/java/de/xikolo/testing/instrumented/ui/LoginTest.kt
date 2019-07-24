package de.xikolo.testing.instrumented.ui


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import de.xikolo.R
import de.xikolo.controllers.login.LoginActivity
import de.xikolo.managers.UserManager
import de.xikolo.testing.instrumented.mocking.base.BaseMockedTest
import de.xikolo.testing.instrumented.ui.helper.AuthorizationHelper
import de.xikolo.testing.instrumented.ui.helper.NavigationHelper.WAIT_LOADING_SHORT
import junit.framework.Assert.assertTrue
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@LargeTest
class LoginTest : BaseMockedTest() {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(LoginActivity::class.java)

    /**
     * Logs the user out.
     */
    @Before
    @After
    fun logout() {
        if (UserManager.isAuthorized) {
            UserManager.logout()
        }
    }

    /**
     * Tests the behavior of the login page.
     * Any credentials are typed in (because the API request is mocked anyways) and the 'login' button is pressed.
     * After a specific timeout it is asserted that the user is logged in.
     */
    @Test
    fun loginTest() {
        val emailView = onView(
            allOf(
                withId(R.id.editEmail),
                isDisplayed()
            )
        )

        emailView.perform(
            clearText(),
            typeText(AuthorizationHelper.EMAIL)
        )

        val passwordView = onView(
            allOf(
                withId(R.id.editPassword),
                isDisplayed()
            )
        )

        passwordView.perform(
            clearText(),
            typeText(AuthorizationHelper.PASSWORD),
            closeSoftKeyboard()
        )

        val loginButton = onView(
            allOf(
                withId(R.id.btnLogin)
            )
        )

        loginButton.perform(click())

        Thread.sleep(WAIT_LOADING_SHORT) // necessary waiting

        assertTrue(UserManager.isAuthorized)
    }

}
