package de.xikolo.ui


import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import de.xikolo.controllers.main.SplashActivity
import de.xikolo.mocking.base.BaseMockedTest
import de.xikolo.ui.helper.AssertionHelper
import de.xikolo.ui.helper.NavigationHelper.Companion.WAIT_STARTUP
import org.junit.Rule
import org.junit.Test

@LargeTest
class AppStartupTest : BaseMockedTest() {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(SplashActivity::class.java)

    /**
     * Tests the app startup process by asserting the main view is shown after a specific timeout.
     */
    @Test
    fun appStartupTest() {
        Thread.sleep(WAIT_STARTUP)

        AssertionHelper.assertMainShown()
    }

}
