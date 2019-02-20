package de.xikolo.mocking.base

import androidx.test.platform.app.InstrumentationRegistry
import de.xikolo.mocking.MockingInterceptor
import de.xikolo.network.ApiService
import org.junit.After
import org.junit.Before

open class BaseMockedTest {

    /**
     * Adds the mocking interceptor to the ApiService class statically to satisfy API requests locally.
     */
    @Before
    fun enableMocking() {
        ApiService.enableMocking(
            MockingInterceptor(
                InstrumentationRegistry.getInstrumentation().context
            )
        )
    }

    /**
     * Removes the mocking interceptor from the ApiService class.
     */
    @After
    fun disableMocking() {
        ApiService.disableMocking()
    }

}
