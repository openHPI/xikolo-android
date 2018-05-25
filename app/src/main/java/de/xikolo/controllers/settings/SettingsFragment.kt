package de.xikolo.controllers.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v14.preference.PreferenceFragment
import android.support.v4.content.ContextCompat
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceCategory
import android.support.v7.preference.PreferenceScreen
import de.psdev.licensesdialog.LicensesDialog
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.events.LoginEvent
import de.xikolo.events.LogoutEvent
import de.xikolo.managers.UserManager
import de.xikolo.utils.ToastUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class SettingsFragment : PreferenceFragment() {

    companion object {
        val TAG: String = SettingsFragment::class.java.simpleName

        fun newInstance(): PreferenceFragment {
            return SettingsFragment()
        }
    }

    private var loginOut: Preference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())
        EventBus.getDefault().register(this)
    }

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        if (Build.VERSION.SDK_INT < 23) {
            val screen = findPreference(getString(R.string.preference_screen)) as PreferenceScreen
            val video = findPreference(getString(R.string.preference_category_video_playback_speed)) as PreferenceCategory
            val videoPlaybackSpeed = findPreference(getString(R.string.preference_video_playback_speed))
            video.removePreference(videoPlaybackSpeed)
            screen.removePreference(video)
        }

        val copyright = findPreference(getString(R.string.preference_copyright))
        copyright.title = String.format(copyright.title.toString(), Calendar.getInstance().get(Calendar.YEAR))
        copyright.setOnPreferenceClickListener { _ ->
            openUrl(Config.COPYRIGHT_URL)
            true
        }

        val imprint = findPreference(getString(R.string.preference_imprint))
        if (Config.IMPRINT_URL != null) {
            imprint.setOnPreferenceClickListener { _ ->
                openUrl(Config.IMPRINT_URL)
                true
            }
        } else {
            val info = findPreference(getString(R.string.preference_category_info)) as PreferenceCategory
            info.removePreference(imprint)
        }

        val privacy = findPreference(getString(R.string.preference_privacy))
        if (Config.PRIVACY_URL != null) {
            privacy.setOnPreferenceClickListener { _ ->
                openUrl(Config.PRIVACY_URL)
                true
            }
        } else {
            val info = findPreference(getString(R.string.preference_category_info)) as PreferenceCategory
            info.removePreference(privacy)
        }

        val termsOfUse = findPreference(getString(R.string.preference_terms_of_use))
        if (Config.TERMS_OF_USE_URL != null) {
            termsOfUse.setOnPreferenceClickListener { _ ->
                openUrl(Config.TERMS_OF_USE_URL)
                true
            }
        } else {
            val info = findPreference(getString(R.string.preference_category_info)) as PreferenceCategory
            info.removePreference(termsOfUse)
        }

        val buildVersion = findPreference(getString(R.string.preference_build_version))
        buildVersion.summary = (buildVersion.summary.toString()
                + " "
                + BuildConfig.VERSION_NAME)

        val licenses = findPreference(getString(R.string.preference_open_source_licenses))
        licenses.setOnPreferenceClickListener { _ ->
            LicensesDialog.Builder(activity)
                .setNotices(R.raw.notices)
                .setTitle(R.string.settings_title_licenses)
                .build()
                .show()
            true
        }

        val sendFeedback = findPreference(getString(R.string.preference_send_feedback))
        sendFeedback.setOnPreferenceClickListener { _ ->
            startFeedbackIntent()
            true
        }

        loginOut = findPreference(getString(R.string.preference_login_out))
        if (UserManager.isAuthorized) {
            buildLogoutView(loginOut)
        } else {
            buildLoginView(loginOut)
        }
    }

    private fun buildLoginView(pref: Preference?) {
        if (pref != null) {
            pref.title = getString(R.string.login)
            pref.setOnPreferenceClickListener { _ ->
                val intent = LoginActivityAutoBundle.builder().build(activity)
                startActivity(intent)
                true
            }
        }
    }

    private fun buildLogoutView(pref: Preference?) {
        if (pref != null) {
            pref.title = getString(R.string.logout)
            pref.setOnPreferenceClickListener { _ ->
                UserManager.logout()
                ToastUtil.show(R.string.toast_successful_logout);
                true
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLoginEvent(event: LoginEvent) {
        buildLogoutView(loginOut)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onLogoutEvent(event: LogoutEvent) {
        buildLoginView(loginOut)
    }

    private fun openUrl(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setToolbarColor(ContextCompat.getColor(activity, R.color.apptheme_main))
            .build()
        customTabsIntent.launchUrl(activity, Uri.parse(url))
    }

    private fun startFeedbackIntent() {
        val osVersion = Build.VERSION.RELEASE.toString()
        val deviceName = String.format("%s %s", Build.MANUFACTURER, Build.MODEL)
        val brand = resources.getString(R.string.app_name)
        val versionName = BuildConfig.VERSION_NAME
        val buildId = BuildConfig.VERSION_CODE.toString()

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(
            Intent.EXTRA_EMAIL,
            arrayOf(resources.getString(R.string.settings_send_app_feedback_mail_address))
        )
        intent.putExtra(
            Intent.EXTRA_SUBJECT,
            String.format(resources.getString(R.string.settings_send_app_feedback_subject), brand)
        )
        intent.putExtra(
            Intent.EXTRA_TEXT,
            String.format(
                resources.getString(R.string.settings_send_app_feedback_message),
                osVersion,
                deviceName,
                brand,
                versionName,
                buildId
            )
        )

        startActivity(
            Intent.createChooser(
                intent,
                resources.getString(R.string.settings_send_app_feedback)
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

}
