package de.xikolo.network

import de.xikolo.models.AccessToken
import de.xikolo.models.Announcement
import de.xikolo.models.Channel
import de.xikolo.models.Course
import de.xikolo.models.CourseDate
import de.xikolo.models.CourseProgress
import de.xikolo.models.Document
import de.xikolo.models.Enrollment
import de.xikolo.models.Item
import de.xikolo.models.Quiz
import de.xikolo.models.QuizSubmission
import de.xikolo.models.Section
import de.xikolo.models.Ticket
import de.xikolo.models.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HEAD
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiServiceInterface {

    // Base Request for Health Checks on Startup

    @HEAD("./")
    fun base(): Call<Void>

    //Channel

    @GET("channels?include=courses")
    fun listChannelsWithCourses(): Call<Array<Channel.JsonModel>>

    @GET("channels/{id}?include=courses")
    fun getChannelWithCourses(@Path("id") id: String): Call<Channel.JsonModel>

    // Course

    @GET("courses")
    fun listCourses(): Call<Array<Course.JsonModel>>

    @GET("courses?include=user_enrollment")
    fun listCoursesWithEnrollments(): Call<Array<Course.JsonModel>>

    @GET("courses/{id}")
    fun getCourse(@Path("id") id: String): Call<Course.JsonModel>

    @GET("courses/{id}?include=user_enrollment")
    fun getCourseWithEnrollment(@Path("id") id: String): Call<Course.JsonModel>

    @GET("courses/{id}?include=sections")
    fun getCourseWithSections(@Path("id") id: String): Call<Course.JsonModel>

    // Enrollment

    @GET("enrollments")
    fun listEnrollments(): Call<Array<Enrollment.JsonModel>>

    @POST("enrollments")
    fun createEnrollment(@Body enrollment: Enrollment.JsonModel): Call<Enrollment.JsonModel>

    @PATCH("enrollments/{id}")
    fun updateEnrollment(@Path("id") id: String, @Body enrollment: Enrollment.JsonModel): Call<Enrollment.JsonModel>

    @DELETE("enrollments/{id}")
    fun deleteEnrollment(@Path("id") id: String): Call<Void>

    // Section

    @GET("course-sections?include=items")
    fun listSectionsWithItemsForCourse(@Query("filter[course]") courseId: String): Call<Array<Section.JsonModel>>

    // Item

    @GET("course-items/{id}?include=content")
    fun getItemWithContent(@Path("id") id: String): Call<Item.JsonModel>

    @GET("course-items?include=content")
    fun listItemsWithContentForSection(@Query("filter[section]") sectionId: String): Call<Array<Item.JsonModel>>

    @PATCH("course-items/{id}")
    fun updateItem(@Path("id") id: String, @Body item: Item.JsonModel): Call<Item.JsonModel>

    // Quiz

    @GET("quizzes/{id}?include=questions,newest_user_submission")
    fun getQuizWithQuestionsAndSubmission(@Path("id") id: String): Call<Quiz.JsonModel>

    @POST("quiz-submissions")
    fun createQuizSubmission(
        @Body submission: QuizSubmission.JsonModel
    ): Call<QuizSubmission.JsonModel>

    @PATCH("quiz-submissions/{id}")
    fun updateQuizSubmission(
        @Path("id") id: String,
        @Body submission: QuizSubmission.JsonModel
    ): Call<QuizSubmission.JsonModel>

    // Progress

    @GET("course-progresses/{id}?include=section-progresses")
    fun getCourseProgressWithSections(@Path("id") id: String): Call<CourseProgress.JsonModel>

    // Dates

    @GET("course-dates")
    fun listDates(): Call<Array<CourseDate.JsonModel>>

    // User and Profile

    @GET("users/me?include=profile")
    fun getUserWithProfile(): Call<User.JsonModel>

    // Announcement

    @GET("announcements?filter[global]=true")
    fun listGlobalAnnouncements(): Call<Array<Announcement.JsonModel>>

    @GET("announcements")
    fun listCourseAnnouncements(@Query("filter[course]") courseId: String): Call<Array<Announcement.JsonModel>>

    @GET("announcements/{id}")
    fun getAnnouncement(@Path("id") id: String): Call<Announcement.JsonModel>

    @PATCH("announcements/{id}")
    fun updateAnnouncement(@Path("id") id: String, @Body item: Announcement.JsonModel): Call<Announcement.JsonModel>

    // Documents

    @GET("documents?include=localizations,courses")
    fun listDocumentsWithLocalizationsForCourse(@Query("filter[course]") courseId: String): Call<Array<Document.JsonModel>>

    // Token

    @FormUrlEncoded
    @POST("authenticate")
    fun createToken(@Field("email") email: String, @Field("password") password: String): Call<AccessToken>

    // Ticket

    @POST("tickets")
    fun createTicket(@Body ticket: Ticket.JsonModel): Call<Ticket.JsonModel>

}
