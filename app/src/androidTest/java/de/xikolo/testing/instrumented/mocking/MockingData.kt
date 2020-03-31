package de.xikolo.testing.instrumented.mocking

import android.content.Context
import de.xikolo.testing.instrumented.mocking.base.BaseMockedResponse
import okhttp3.Request

class MockingData {
    companion object {

        /**
         * Returns a mocked response object based on the {@code request} supplied.
         * Returns null if request cannot be mocked and the {@link MockingInterceptor} should proceed without mocking.
         */
        fun getResponse(context: Context, request: Request): BaseMockedResponse? {
            return when (request.url.encodedPath) {
                MockedRequest.AUTHENTICATE.path -> MockedResponseFromJsonAsset(context, "mockdata/authenticate")
                MockedRequest.COURSES.path      -> MockedResponseFromJsonAsset(context, "mockdata/courses")
                MockedRequest.CHANNELS.path     -> MockedResponseFromJsonAsset(context, "mockdata/channels")
                MockedRequest.USERS_ME.path     -> MockedResponseFromJsonAsset(context, "mockdata/users/me")
                MockedRequest.COURSE_DATES.path -> MockedResponseFromJsonAsset(context, "mockdata/course-dates")
                else                            -> null
            }
        }
    }

}
