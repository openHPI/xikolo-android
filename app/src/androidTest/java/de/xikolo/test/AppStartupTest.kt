package de.xikolo.test


import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import de.xikolo.controllers.main.SplashActivity
import de.xikolo.test.util.AssertionHelper
import de.xikolo.test.util.NavigationHelper.Companion.WAIT_STARTUP
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class AppStartupTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(SplashActivity::class.java)

    @Test
    fun appStartupTest() {
        // wait for app startup
        Thread.sleep(WAIT_STARTUP)

        AssertionHelper.assertMainShown()
    }

}
