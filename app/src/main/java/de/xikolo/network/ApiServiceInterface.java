package de.xikolo.network;

import de.xikolo.models.AccessToken;
import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.models.Profile;
import de.xikolo.utils.Config;
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
import retrofit2.http.Url;

public interface ApiServiceInterface {

    @GET("courses")
    Call<Course.JsonModel[]> listCourses();

    @GET("courses?include=user_enrollment")
    Call<Course.JsonModel[]> listCoursesWithEnrollments(@Header(Config.HEADER_AUTHORIZATION) String token);

    @GET("courses/{id}")
    Call<Course.JsonModel> getCourse(@Path("id") String id);

    @GET("courses/{id}")
    Call<Course.JsonModel> getCourseWithEnrollment(@Header(Config.HEADER_AUTHORIZATION) String token, @Path("id") String id);

    @GET("enrollments")
    Call<Enrollment.JsonModel[]> listEnrollments(@Header(Config.HEADER_AUTHORIZATION) String token);

    @POST("enrollments")
    Call<Enrollment.JsonModel> createEnrollment(@Header(Config.HEADER_AUTHORIZATION) String token, @Body Enrollment.JsonModel enrollment);

    @PATCH("enrollments/{id}")
    Call<Enrollment.JsonModel> updateEnrollment(@Header(Config.HEADER_AUTHORIZATION) String token, @Path("id") String id, @Body Enrollment.JsonModel enrollment);

    @DELETE("enrollments/{id}")
    Call deleteEnrollment(@Header(Config.HEADER_AUTHORIZATION) String token, @Path("id") String id);

    @GET("profiles/{id}")
    Call<Profile.JsonModel> getProfile(@Header(Config.HEADER_AUTHORIZATION) String token, @Path("id") String id);

    @FormUrlEncoded
    @POST
    Call<AccessToken> createToken(@Url String url, @Field("email") String email, @Field("password") String password);

}
