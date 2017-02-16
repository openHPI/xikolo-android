package de.xikolo.network;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.utils.Config;
import moe.banana.jsonapi2.ResourceAdapterFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class ApiV2Request {

    private static ApiService service;

    static {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (Config.DEBUG) {
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        } else {
            logging.setLevel(HttpLoggingInterceptor.Level.NONE);
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Interceptor.Chain chain) throws IOException {
                        Request original = chain.request();

                        Request request = original.newBuilder()
                                .header(Config.HEADER_ACCEPT, Config.HEADER_ACCEPT_VALUE_API_V2)
                                .header(Config.HEADER_USER_PLATFORM, Config.HEADER_USER_PLATFORM_VALUE)
                                .build();

                        return chain.proceed(request);
                    }
                })
                .addInterceptor(logging)
                .build();

        JsonAdapter.Factory jsonApiAdapterFactory = ResourceAdapterFactory.builder()
                .add(Course.JsonModel.class)
                .add(Enrollment.JsonModel.class)
                .build();

        Moshi moshi = new Moshi.Builder()
                .add(jsonApiAdapterFactory)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.API_V2)
                .client(client)
                .addConverterFactory(JsonApiConverterFactory.create(moshi))
                .build();

        service = retrofit.create(ApiService.class);
    }

    public static ApiService service() {
        return service;
    }

}
