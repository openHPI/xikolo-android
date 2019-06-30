package de.xikolo.testing.instrumented.unit

import android.content.Intent
import androidx.test.filters.SmallTest
import androidx.test.rule.ActivityTestRule
import de.xikolo.controllers.base.ViewModelCreationInterface
import de.xikolo.controllers.main.SplashActivity
import de.xikolo.testing.instrumented.mocking.base.BaseMockedTest
import de.xikolo.viewmodels.base.BaseViewModel
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@SmallTest
class ViewModelTest : BaseMockedTest(), ViewModelCreationInterface<ViewModelTest.TestViewModel> {

    @Rule
    @JvmField
    var activityTestRule = ActivityTestRule(SplashActivity::class.java, false, false)

    override lateinit var viewModel: TestViewModel

    override fun createViewModel(): TestViewModel {
        return TestViewModel()
    }

    @Before
    fun create() {
        activityTestRule.launchActivity(
            Intent(context, SplashActivity::class.java)
        )

        initViewModel(activityTestRule.activity)
        viewModel.onCreate()
    }

    @Test
    fun testInitialization() {
        assertNotNull(viewModel)
        activityTestRule.activity.runOnUiThread {
            assertNotNull(viewModel.realm)
        }
        assertNotNull(viewModel.networkState)
    }

    @Test
    fun testInterface() {
        assertTrue(viewModel.createCalled)
        assertTrue(viewModel.firstCreateCalled)
    }

    @Test
    fun testRefreshing() {
        assertTrue(viewModel.refreshCount == 0)
        viewModel.onRefresh()
        assertTrue(viewModel.refreshCount == 1)
        viewModel.onRefresh()
        assertTrue(viewModel.refreshCount == 2)
    }

    class TestViewModel : BaseViewModel() {

        var refreshCount = 0
        var firstCreateCalled: Boolean = false
        var createCalled: Boolean = false

        override fun onCreate() {
            super.onCreate()
            createCalled = true
        }

        override fun onFirstCreate() {
            super.onFirstCreate()
            firstCreateCalled = true
        }

        override fun onRefresh() {
            refreshCount++
        }
    }
}
