package de.xikolo.network;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import de.xikolo.App;
import de.xikolo.R;
import de.xikolo.config.Config;
import de.xikolo.managers.UserManager;
import de.xikolo.models.Announcement;
import de.xikolo.models.Channel;
import de.xikolo.models.Course;
import de.xikolo.models.CourseProgress;
import de.xikolo.models.Document;
import de.xikolo.models.DocumentLocalization;
import de.xikolo.models.Enrollment;
import de.xikolo.models.Item;
import de.xikolo.models.LtiExercise;
import de.xikolo.models.PeerAssessment;
import de.xikolo.models.Profile;
import de.xikolo.models.Quiz;
import de.xikolo.models.RichText;
import de.xikolo.models.Section;
import de.xikolo.models.SectionProgress;
import de.xikolo.models.SubtitleCue;
import de.xikolo.models.SubtitleTrack;
import de.xikolo.models.Video;
import moe.banana.jsonapi2.JsonApiConverterFactory;
import moe.banana.jsonapi2.ResourceAdapterFactory;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public abstract class ApiService {

    public static final Interceptor authenticationInterceptor = chain -> {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder();
        if (original.url().host().equals(App.getInstance().getString(R.string.app_host)) && UserManager.isAuthorized()) {
            builder.header(Config.HEADER_AUTH, Config.HEADER_AUTH_VALUE_PREFIX_JSON_API + UserManager.getToken());
        }

        return chain.proceed(builder.build());
    };

    public static final Interceptor userAgentInterceptor = chain -> {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder()
            .header(Config.HEADER_USER_AGENT, Config.HEADER_USER_AGENT_VALUE);

        return chain.proceed(builder.build());
    };

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
                .addInterceptor(chain -> {
                    Request original = chain.request();

                    String mediaType = Config.MEDIA_TYPE_JSON_API;
                    String xikoloVersionExtension = "; xikolo-version=" + Config.XIKOLO_API_VERSION;
                    String appLanguage = Locale.getDefault().getLanguage();

                    // plain json calls
                    List plainJson = Arrays.asList(
                        "/api/v2/authenticate"
                    );
                    if (plainJson.contains(original.url().encodedPath())) {
                        mediaType = Config.MEDIA_TYPE_JSON;
                        xikoloVersionExtension = "";
                    }

                    Request.Builder builder = original.newBuilder()
                        .header(Config.HEADER_ACCEPT, mediaType + xikoloVersionExtension)
                        .header(Config.HEADER_CONTENT_TYPE, mediaType)
                        .header(Config.HEADER_USER_PLATFORM, Config.HEADER_USER_PLATFORM_VALUE)
                        .header(Config.HEADER_ACCEPT_LANGUAGE, appLanguage);

                    return chain.proceed(builder.build());
                })
                .addInterceptor(authenticationInterceptor)
                .addInterceptor(userAgentInterceptor)
                .addInterceptor(logging)
                .build();

            JsonAdapter.Factory jsonApiAdapterFactory = ResourceAdapterFactory.builder()
                .add(Course.JsonModel.class)
                .add(Channel.JsonModel.class)
                .add(Section.JsonModel.class)
                .add(Item.JsonModel.class)
                .add(Enrollment.JsonModel.class)
                .add(CourseProgress.JsonModel.class)
                .add(SectionProgress.JsonModel.class)
                .add(Profile.JsonModel.class)
                .add(RichText.JsonModel.class)
                .add(Quiz.JsonModel.class)
                .add(Video.JsonModel.class)
                .add(LtiExercise.JsonModel.class)
                .add(PeerAssessment.JsonModel.class)
                .add(SubtitleTrack.JsonModel.class)
                .add(SubtitleCue.JsonModel.class)
                .add(Announcement.JsonModel.class)
                .add(Document.JsonModel.class)
                .add(DocumentLocalization.JsonModel.class)
                .build();

            Moshi moshi = new Moshi.Builder()
                .add(jsonApiAdapterFactory)
                .build();

            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Config.API_URL)
                .client(client)
                .addConverterFactory(JsonApiConverterFactory.create(moshi))
                .addConverterFactory(MoshiConverterFactory.create())
                .build();

            service = retrofit.create(ApiServiceInterface.class);
        }
        return service;
    }

}
