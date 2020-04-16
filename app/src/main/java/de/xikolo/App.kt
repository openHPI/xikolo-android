package de.xikolo

import android.app.Application
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import android.webkit.CookieManager
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.preference.PreferenceManager
import de.xikolo.config.Config
import de.xikolo.config.Feature
import de.xikolo.controllers.course.CourseActivityAutoBundle
import de.xikolo.lanalytics.Lanalytics
import de.xikolo.models.migrate.RealmSchemaMigration
import de.xikolo.states.ConnectivityStateLiveData
import de.xikolo.states.DownloadStateLiveData
import de.xikolo.states.LoginStateLiveData
import de.xikolo.states.PermissionStateLiveData
import de.xikolo.states.base.LiveDataEvent
import de.xikolo.storages.RecentCoursesStorage
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

    inner class State {

        val connectivity: ConnectivityStateLiveData by lazy {
            ConnectivityStateLiveData(this@App)
        }

        val login: LoginStateLiveData by lazy {
            LoginStateLiveData()
        }

        val downloadCancellation by lazy {
            LiveDataEvent()
        }

        val download: DownloadStateLiveData.Companion by lazy {
            DownloadStateLiveData
        }

        val permission: PermissionStateLiveData.Companion by lazy {
            PermissionStateLiveData
        }
    }

    val state = State()

    val lanalytics: Lanalytics by lazy {
        Lanalytics.getInstance(this, Config.API_URL + Config.LANALYTICS_PATH)
    }

    val clientId: String by lazy {
        ClientUtil.id!!
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()

        configureRealm()
        configureDefaultSettings()
        configureWebView()

        if (Build.VERSION.SDK_INT >= Feature.SHORTCUTS) {
            configureShortcuts()
        }
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

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun configureShortcuts() {
        val recentCoursesObserver = Observer<String> {
            updateShortcuts()
        }

        val recentCoursesStorage = RecentCoursesStorage()
        recentCoursesStorage.coursesLive.observeForever(recentCoursesObserver)
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun updateShortcuts() {
        val shortcutManager = getSystemService<ShortcutManager>(ShortcutManager::class.java)
        val recentCourses = RecentCoursesStorage().recentCourses
        val intentList = mutableListOf<ShortcutInfo>()

        for (course in recentCourses) {
            val courseId = course.first
            val title = course.second

            val intent = CourseActivityAutoBundle.builder().courseId(courseId).build(applicationContext)
            intent.action = Intent.ACTION_VIEW

            val shortcut = ShortcutInfo.Builder(applicationContext, courseId)
                .setShortLabel(title)
                .setIcon(Icon.createWithResource(applicationContext, R.drawable.ic_shortcut_course))
                .setIntent(intent)
                .build()

            intentList.add(shortcut)
        }
        shortcutManager.dynamicShortcuts = intentList
    }
}
