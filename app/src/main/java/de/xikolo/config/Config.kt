package de.xikolo.config

import android.os.Build
import com.google.android.gms.cast.CastMediaControlIntent
import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.utils.DeviceUtil

object Config {

    @JvmField val HOST: String
    @JvmField val HOST_URL: String
    @JvmField val API_URL: String

    @JvmField val XIKOLO_API_VERSION: Int
    @JvmField val REALM_SCHEMA_VERSION: Int

    @JvmField val COPYRIGHT_URL: String
    @JvmField val IMPRINT_URL: String?
    @JvmField val PRIVACY_URL: String?
    @JvmField val TERMS_OF_USE_URL: String?

    @JvmField val CAST_MEDIA_RECEIVER_APPLICATION_ID: String

    init {
        when (BuildConfig.X_FLAVOR) {
            BuildFlavor.OPEN_HPI -> {
                COPYRIGHT_URL = "https://hpi.de/"
                IMPRINT_URL = "https://open.hpi.de/pages/imprint"
                PRIVACY_URL = "https://open.hpi.de/pages/privacy"
                TERMS_OF_USE_URL = null
                CAST_MEDIA_RECEIVER_APPLICATION_ID = "EE6FB604"
            }
            BuildFlavor.OPEN_SAP -> {
                COPYRIGHT_URL = "http://sap.com/corporate-en/legal/copyright/index.epx"
                IMPRINT_URL = "http://sap.com/corporate-en/legal/impressum.epx"
                PRIVACY_URL = "http://sap.com/corporate/en/legal/privacy.html"
                TERMS_OF_USE_URL = "http://sap.com/corporate-en/about/legal/terms-of-use.html"
                CAST_MEDIA_RECEIVER_APPLICATION_ID = "2C63C05D"
            }
            BuildFlavor.OPEN_WHO -> {
                COPYRIGHT_URL = "http://who.int/"
                IMPRINT_URL = null
                PRIVACY_URL = null
                TERMS_OF_USE_URL = "https://openwho.org/pages/terms_of_use"
                CAST_MEDIA_RECEIVER_APPLICATION_ID =
                    CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
            }
            else -> { // MOOC_HOUSE
                COPYRIGHT_URL = "https://hpi.de/"
                IMPRINT_URL = "https://mooc.house/pages/imprint"
                PRIVACY_URL = "https://mooc.house/pages/privacy"
                TERMS_OF_USE_URL = null
                CAST_MEDIA_RECEIVER_APPLICATION_ID =
                    CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID
            }
        }

        HOST = App.instance.getString(de.xikolo.R.string.app_host)
        HOST_URL = "https://$HOST/"
        API_URL = HOST_URL + "api/v2/"

        XIKOLO_API_VERSION = App.instance.resources.getInteger(de.xikolo.R.integer.xikolo_api_version)
        REALM_SCHEMA_VERSION = App.instance.resources.getInteger(de.xikolo.R.integer.realm_schema_version)
    }

    @JvmField val DEBUG = BuildConfig.X_TYPE === BuildType.DEBUG

    const val HEADER_ACCEPT = "Accept"
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
    const val HEADER_USER_AGENT = "User-Agent"

    const val MEDIA_TYPE_JSON = "application/json"
    const val MEDIA_TYPE_JSON_API = "application/vnd.api+json"

    const val HEADER_AUTH = "Authorization"
    const val HEADER_AUTH_VALUE_PREFIX = "Token token="
    const val HEADER_AUTH_VALUE_PREFIX_JSON_API = "Legacy-Token token="

    const val HEADER_USER_PLATFORM = "X-User-Platform"
    const val HEADER_USER_PLATFORM_VALUE = "Android"
    @JvmField val HEADER_USER_AGENT_VALUE =
        "${App.instance.resources.getString(de.xikolo.R.string.app_name)}/${BuildConfig.VERSION_NAME} Android/${Build.VERSION.RELEASE} (${DeviceUtil.deviceName})"

    const val HEADER_API_VERSION_EXPIRATION_DATE = "X-Api-Version-Expiration-Date"

    const val FONT_DIR = "fonts/"
    const val FONT_XIKOLO = "xikolo.ttf"
    const val FONT_MATERIAL = "materialdesign.ttf"

    const val LANALYTICS_CONTEXT_COOKIE = "lanalytics-context"
    const val LANALYTICS_PATH = "tracking-events/"

    const val ACCOUNT = "account/"
    const val NEW = "new/"
    const val RESET = "reset/$NEW"
    const val COURSES = "courses/"
    const val DISCUSSIONS = "pinboard/"
    const val ITEMS = "items/"
    const val RECAP = "learn?course_id="

    const val PRESENTER_LIFECYCLE_LOGGING = false
    const val WEBVIEW_LOGGING = false

}
