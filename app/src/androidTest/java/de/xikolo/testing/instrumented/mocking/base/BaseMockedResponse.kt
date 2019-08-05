package de.xikolo.testing.instrumented.mocking.base

/**
 * Represents the data of a mocked response from the ApiService.
 */
abstract class BaseMockedResponse {

    abstract var responseString: String
        protected set

    abstract var statusCode: Int
        protected set

    abstract var contentType: String
        protected set

}
