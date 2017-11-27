package de.xikolo.network;

import de.xikolo.models.AccessToken;
import de.xikolo.models.Announcement;
import de.xikolo.models.Course;
import de.xikolo.models.CourseProgress;
import de.xikolo.models.Enrollment;
import de.xikolo.models.Item;
import de.xikolo.models.Section;
import de.xikolo.models.SubtitleTrack;
import de.xikolo.models.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiServiceInterface {

    // Base Request for Health Checks on Startup

    // TODO refactor to base route when fixed in API
    @HEAD("channels")
    Call<Void> base();

    // Course

    @GET("courses")
    Call<Course.JsonModel[]> listCourses();

    @GET("courses?include=user_enrollment")
    Call<Course.JsonModel[]> listCoursesWithEnrollments();

    @GET("courses/{id}")
    Call<Course.JsonModel> getCourse(@Path("id") String id);

    @GET("courses/{id}?include=user_enrollment")
    Call<Course.JsonModel> getCourseWithEnrollment(@Path("id") String id);

    @GET("courses/{id}?include=sections")
    Call<Course.JsonModel> getCourseWithSections(@Path("id") String id);

    // Enrollment

    @GET("enrollments")
    Call<Enrollment.JsonModel[]> listEnrollments();

    @POST("enrollments")
    Call<Enrollment.JsonModel> createEnrollment(@Body Enrollment.JsonModel enrollment);

    @PATCH("enrollments/{id}")
    Call<Enrollment.JsonModel> updateEnrollment(@Path("id") String id, @Body Enrollment.JsonModel enrollment);

    @DELETE("enrollments/{id}")
    Call<Void> deleteEnrollment(@Path("id") String id);

    // Section

    @GET("course-sections?include=items")
    Call<Section.JsonModel[]> listSectionsWithItemsForCourse(@Query("filter[course]") String courseId);

    // Item

    @GET("course-items/{id}?include=content")
    Call<Item.JsonModel> getItemWithContent(@Path("id") String id);

    @GET("course-items?include=content")
    Call<Item.JsonModel[]> listItemsWithContentForSection(@Query("filter[section]") String sectionId);

    @PATCH("course-items/{id}")
    Call<Item.JsonModel> updateItem(@Path("id") String id, @Body Item.JsonModel item);

    // Progress

    @GET("course-progresses/{id}?include=section-progresses")
    Call<CourseProgress.JsonModel> getCourseProgressWithSections(@Path("id") String id);

    // Subtitle

    @GET("subtitle-tracks?include=cues")
    Call<SubtitleTrack.JsonModel[]> listSubtitlesWithCuesForVideo(@Query("filter[video]") String videoId);

    // User and Profile

    @GET("users/me?include=profile")
    Call<User.JsonModel> getUserWithProfile();

    // Announcement

    @GET("announcements?filter[global]=true")
    Call<Announcement.JsonModel[]> listGlobalAnnouncements();

    @GET("announcements?filter[global]=true")
    Call<Announcement.JsonModel[]> listGlobalAnnouncementsWithCourses();

    @GET("announcements")
    Call<Announcement.JsonModel[]> listCourseAnnouncements(@Query("filter[course]") String courseId);

    @PATCH("announcements/{id}")
    Call<Announcement.JsonModel> updateAnnouncement(@Path("id") String id, @Body Announcement.JsonModel item);

    // Token

    @FormUrlEncoded
    @POST("authenticate")
    Call<AccessToken> createToken(@Field("email") String email, @Field("password") String password);

}
