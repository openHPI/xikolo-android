package de.xikolo.config

import android.os.Build
import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.utils.DeviceUtil

object Config {

    @JvmField val HOST: String
    @JvmField val HOST_URL: String
    @JvmField val API_URL: String

    @JvmField val XIKOLO_API_VERSION: Int
    @JvmField val REALM_SCHEMA_VERSION: Int

    init {
        HOST = App.instance.getString(R.string.app_host)
        HOST_URL = "https://$HOST/"
        API_URL = HOST_URL + "api/v2/"

        XIKOLO_API_VERSION = App.instance.resources.getInteger(de.xikolo.R.integer.xikolo_api_version)
        REALM_SCHEMA_VERSION = App.instance.resources.getInteger(de.xikolo.R.integer.realm_schema_version)
    }

    @JvmField val DEBUG = BuildConfig.BUILD_TYPE == "debug"
    const val RELEASE = BuildConfig.BUILD_TYPE == "release"

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

    const val WEBVIEW_LOGGING = false

}
