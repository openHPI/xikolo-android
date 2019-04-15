package de.xikolo

import android.app.Application
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.preference.PreferenceManager
import de.xikolo.config.Config
import de.xikolo.config.FeatureConfig
import de.xikolo.lanalytics.Lanalytics
import de.xikolo.managers.SecondScreenManager
import de.xikolo.managers.WebSocketManager
import de.xikolo.models.migrate.RealmSchemaMigration
import de.xikolo.utils.ClientUtil
import io.realm.Realm
import io.realm.RealmConfiguration

class App : Application() {

    companion object {
        val TAG: String = App::class.java.simpleName

        @JvmStatic
        lateinit var instance: App
            private set
    }

    val lanalytics: Lanalytics by lazy {
        Lanalytics.getInstance(this, Config.API_URL + Config.LANALYTICS_PATH)
    }

    val webSocketManager by lazy {
        WebSocketManager(Config.WEBSOCKET_URL)
    }

    val clientId: String by lazy {
        ClientUtil.id(this)
    }

    private var secondScreenManager: SecondScreenManager? = null

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        configureRealm()
        configureDefaultSettings()
        configureWebView()
        configureSecondScreenManager()
    }

    private fun configureRealm() {
        Realm.init(this)
        val config = RealmConfiguration.Builder()
            .schemaVersion(Config.REALM_SCHEMA_VERSION.toLong())
            .migration(RealmSchemaMigration())
            .build()
        Realm.setDefaultConfiguration(config)
    }

    private fun configureDefaultSettings() {
        PreferenceManager.setDefaultValues(this, R.xml.settings, false)
    }

    private fun configureWebView() {
        CookieManager.getInstance().setAcceptCookie(true)

        if (Config.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }

    fun syncCookieSyncManager() {
        CookieManager.getInstance().flush()
    }

    fun clearCookieSyncManager() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().removeSessionCookies(null)
        CookieManager.getInstance().flush()
    }

    @Synchronized
    fun configureSecondScreenManager() {
        if (FeatureConfig.SECOND_SCREEN) {
            if (secondScreenManager == null) {
                secondScreenManager = SecondScreenManager()
            }
        }
    }

}
