package de.xikolo.network;

import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.utils.Config;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @GET("courses")
    Call<Course.JsonModel[]> listCourses();

    @GET("courses?include=user_enrollment")
    Call<Course.JsonModel[]> listCoursesWithEnrollments(@Header(Config.HEADER_AUTHORIZATION) String token);

    @GET("courses/{id}")
    Call<Course.JsonModel> getCourse(@Path("id") String id);

    @GET("courses/{id}")
    Call<Course.JsonModel> getCourseWithEnrollment(@Header(Config.HEADER_AUTHORIZATION) String token, @Path("id") String id);

    @POST("enrollments")
    Call<Enrollment.JsonModel> postEnrollment(@Header(Config.HEADER_AUTHORIZATION) String token, @Body Enrollment.JsonModel enrollment);

    @PATCH("enrollments/{id}")
    Call<Enrollment.JsonModel> patchEnrollment(@Header(Config.HEADER_AUTHORIZATION) String token, @Path("id") String id, @Body Enrollment.JsonModel enrollment);

    @DELETE("enrollments/{id}")
    Call deleteEnrollment(@Header(Config.HEADER_AUTHORIZATION) String token, @Path("id") String id);

}
