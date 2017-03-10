package de.xikolo.network;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;

import de.xikolo.models.Course;
import de.xikolo.models.Enrollment;
import de.xikolo.models.Profile;
import de.xikolo.utils.Config;
import moe.banana.jsonapi2.ResourceAdapterFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public abstract class ApiService {

    private static ApiServiceInterface service;

    private ApiService() {
    }

    public synchronized static ApiServiceInterface getInstance() {
        if (service == null) {
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
                    .add(Profile.JsonModel.class)
                    .build();

            Moshi moshi = new Moshi.Builder()
                    .add(jsonApiAdapterFactory)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(Config.API_V2)
                    .client(client)
                    .addConverterFactory(JsonApiConverterFactory.create(moshi))
                    .addConverterFactory(MoshiConverterFactory.create())
                    .build();

            service = retrofit.create(ApiServiceInterface.class);
        }
        return service;
    }

}
