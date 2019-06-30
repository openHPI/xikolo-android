package de.xikolo.testing.instrumented.mocking

/**
 * Holder of requests which might be mocked.
 * By now requests are only defined by a uri path. (This might change)
 */
enum class MockedRequest(val path: String) {
    AUTHENTICATE("/api/v2/authenticate"),
    COURSES("/api/v2/courses"),
    CHANNELS("/api/v2/channels"),
    USERS_ME("/api/v2/users/me"),
    COURSE_DATES("/api/v2/course-dates")
}
