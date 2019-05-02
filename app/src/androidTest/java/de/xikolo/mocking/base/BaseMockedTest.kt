package de.xikolo.mocking.base

import de.xikolo.mocking.MockingInterceptor
import de.xikolo.network.ApiService
import org.junit.After
import org.junit.Before

open class BaseMockedTest : BaseTest() {

    /**
     * Adds the mocking interceptor to the ApiService class statically to satisfy API requests locally.
     */
    @Before
    fun enableMocking() {
        val mockingInterceptor = MockingInterceptor(testContext)

        ApiService.setupInstance(
            ApiService.buildHttpClient()
                .newBuilder()
                .addInterceptor(mockingInterceptor)
                .build()
        )
    }

    /**
     * Removes the mocking interceptor from the ApiService class.
     */
    @After
    fun disableMocking() {
        ApiService.invalidateInstance()
    }

}
