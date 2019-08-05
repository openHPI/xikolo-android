package de.xikolo.network

import com.squareup.moshi.Moshi
import de.xikolo.App
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.managers.UserManager
import de.xikolo.models.*
import moe.banana.jsonapi2.JsonApiConverterFactory
import moe.banana.jsonapi2.ResourceAdapterFactory
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

object ApiService {

    private const val HTTP_CACHE_SIZE_BYTES: Long = 1024 * 1024 * 2

    val authenticationInterceptor = { chain: Interceptor.Chain ->
        val original = chain.request()

        val builder = original.newBuilder()
        if (original.url.host == App.instance.getString(R.string.app_host) && UserManager.isAuthorized) {
            builder.header(
                Config.HEADER_AUTH,
                Config.HEADER_AUTH_VALUE_PREFIX_JSON_API + UserManager.token!!
            )
        }

        chain.proceed(builder.build())
    }

    val userAgentInterceptor = { chain: Interceptor.Chain ->
        val original = chain.request()

        val builder = original.newBuilder()
            .header(Config.HEADER_USER_AGENT, Config.HEADER_USER_AGENT_VALUE)

        chain.proceed(builder.build())
    }

    private var service: ApiServiceInterface? = null

    @JvmStatic
    val instance: ApiServiceInterface
        @Synchronized get() {
            if (service == null) {
                setupInstance()
            }
            return service!!
        }

    @Synchronized
    fun invalidateInstance() {
        service = null
    }

    @Synchronized
    fun setupInstance() {
        setupInstance(buildHttpClient())
    }

    @Synchronized
    fun setupInstance(client: OkHttpClient) {
        service = buildInstance(client)
    }

    @Synchronized
    fun buildInstance(client: OkHttpClient): ApiServiceInterface {
        val jsonApiAdapterFactory = ResourceAdapterFactory.builder()
            .add(Course.JsonModel::class.java)
            .add(Channel.JsonModel::class.java)
            .add(Section.JsonModel::class.java)
            .add(Item.JsonModel::class.java)
            .add(Enrollment.JsonModel::class.java)
            .add(CourseProgress.JsonModel::class.java)
            .add(SectionProgress.JsonModel::class.java)
            .add(Profile.JsonModel::class.java)
            .add(RichText.JsonModel::class.java)
            .add(Quiz.JsonModel::class.java)
            .add(Video.JsonModel::class.java)
            .add(LtiExercise.JsonModel::class.java)
            .add(PeerAssessment.JsonModel::class.java)
            .add(SubtitleTrack.JsonModel::class.java)
            .add(SubtitleCue.JsonModel::class.java)
            .add(Announcement.JsonModel::class.java)
            .add(Document.JsonModel::class.java)
            .add(DocumentLocalization.JsonModel::class.java)
            .add(CourseDate.JsonModel::class.java)
            .build()

        val moshi = Moshi.Builder()
            .add(jsonApiAdapterFactory)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(Config.API_URL)
            .client(client)
            .addConverterFactory(JsonApiConverterFactory.create(moshi))
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        return retrofit.create(ApiServiceInterface::class.java)
    }

    @Synchronized
    fun buildHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        if (Config.DEBUG) {
            logging.level = HttpLoggingInterceptor.Level.BASIC
        } else {
            logging.level = HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()

                var mediaType = Config.MEDIA_TYPE_JSON_API
                var xikoloVersionExtension = "; xikolo-version=" + Config.XIKOLO_API_VERSION
                val appLanguage = Locale.getDefault().language

                // plain json calls
                val plainJson = Arrays.asList(
                    "/api/v2/authenticate"
                )
                if (plainJson.contains(original.url.encodedPath)) {
                    mediaType = Config.MEDIA_TYPE_JSON
                    xikoloVersionExtension = ""
                }

                val builder = original.newBuilder()
                    .header(Config.HEADER_ACCEPT, mediaType + xikoloVersionExtension)
                    .header(Config.HEADER_CONTENT_TYPE, mediaType)
                    .header(Config.HEADER_USER_PLATFORM, Config.HEADER_USER_PLATFORM_VALUE)
                    .header(Config.HEADER_ACCEPT_LANGUAGE, appLanguage)

                chain.proceed(builder.build())
            }
            .addInterceptor(authenticationInterceptor)
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(logging)
            .cache(Cache(App.instance.cacheDir, HTTP_CACHE_SIZE_BYTES))
            .build()
    }

}
