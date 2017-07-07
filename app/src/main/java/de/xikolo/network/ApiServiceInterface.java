package de.xikolo.network;

import de.xikolo.config.Config;
import de.xikolo.models.AccessToken;
import de.xikolo.models.Course;
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
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiServiceInterface {

    // Course

    @GET("courses")
    Call<Course.JsonModel[]> listCourses();

    @GET("courses?include=user_enrollment")
    Call<Course.JsonModel[]> listCoursesWithEnrollments(@Header(Config.HEADER_AUTHORIZATION) String token);

    @GET("courses/{id}")
    Call<Course.JsonModel> getCourse(@Path("id") String id);

    @GET("courses/{id}?include=user_enrollment")
    Call<Course.JsonModel> getCourseWithEnrollment(@Header(Config.HEADER_AUTHORIZATION) String token, @Path("id") String id);

    // Enrollment

    @GET("enrollments")
    Call<Enrollment.JsonModel[]> listEnrollments(@Header(Config.HEADER_AUTHORIZATION) String token);

    @POST("enrollments")
    Call<Enrollment.JsonModel> createEnrollment(@Header(Config.HEADER_AUTHORIZATION) String token, @Body Enrollment.JsonModel enrollment);

    @PATCH("enrollments/{id}")
    Call<Enrollment.JsonModel> updateEnrollment(@Header(Config.HEADER_AUTHORIZATION) String token, @Path("id") String id, @Body Enrollment.JsonModel enrollment);

    @DELETE("enrollments/{id}")
    Call deleteEnrollment(@Header(Config.HEADER_AUTHORIZATION) String token, @Path("id") String id);

    // Section

    @GET("course-sections?include=items")
    Call<Section.JsonModel[]> listSectionsWithItemsForCourse(@Header(Config.HEADER_AUTHORIZATION) String token, @Query("filter[course]") String courseId);

    // Item

    @GET("course-items/{id}?include=content")
    Call<Item.JsonModel> getItemWithContent(@Header(Config.HEADER_AUTHORIZATION) String token, @Path("id") String id);

    @GET("course-items?include=content")
    Call<Item.JsonModel[]> listItemsWithContentForSection(@Header(Config.HEADER_AUTHORIZATION) String token, @Query("filter[section]") String sectionId);

    @PATCH("course-items/{id}")
    Call<Item.JsonModel> updateItem(@Header(Config.HEADER_AUTHORIZATION) String token, @Path("id") String id, @Body Item.JsonModel item);

    // Subtitle
    @GET("subtitles?include=texts")
    Call<SubtitleTrack.JsonModel[]> listSubtitlesWithTextsForVideo(@Header(Config.HEADER_AUTHORIZATION) String token, @Query("filter[video]") String videoId);


    // User and Profile

    @GET("users/me?include=profile")
    Call<User.JsonModel> getUserWithProfile(@Header(Config.HEADER_AUTHORIZATION) String token);

    // Token

    @FormUrlEncoded
    @POST("authenticate")
    Call<AccessToken> createToken(@Field("email") String email, @Field("password") String password);

}
