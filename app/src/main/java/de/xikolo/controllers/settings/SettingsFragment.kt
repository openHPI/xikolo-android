package de.xikolo.controllers.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.psdev.licensesdialog.LicensesDialog
import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.Feature
import de.xikolo.controllers.dialogs.ProgressDialogHorizontal
import de.xikolo.controllers.dialogs.ProgressDialogHorizontalAutoBundle
import de.xikolo.controllers.dialogs.StorageMigrationDialog
import de.xikolo.controllers.dialogs.StorageMigrationDialogAutoBundle
import de.xikolo.controllers.helper.LoginHelper
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.download.filedownload.FileDownloadHandler
import de.xikolo.extensions.observe
import de.xikolo.managers.PermissionManager
import de.xikolo.managers.UserManager
import de.xikolo.models.Storage
import de.xikolo.utils.extensions.asStorageType
import de.xikolo.utils.extensions.fileCount
import de.xikolo.utils.extensions.getString
import de.xikolo.utils.extensions.getStringArray
import de.xikolo.utils.extensions.internalStorage
import de.xikolo.utils.extensions.sdcardStorage
import de.xikolo.utils.extensions.showToast
import de.xikolo.utils.extensions.storages
import java.util.Calendar

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    companion object {
        val TAG: String = SettingsFragment::class.java.simpleName
    }

    private var loginOut: Preference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState ?: Bundle())

        App.instance.state.login
            .observe(this) {
                if (it) {
                    buildLogoutView(loginOut)
                } else {
                    buildLoginView(loginOut)
                }
            }
    }

    override fun onResume() {
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
        refreshPipStatus()
        super.onResume()
    }

    override fun onPause() {
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals(getString(R.string.preference_storage))) {
            val newStoragePreference = sharedPreferences?.getString(getString(R.string.preference_storage), getString(R.string.settings_default_value_storage))!!
            findPreference<ListPreference>(getString(R.string.preference_storage))?.summary = newStoragePreference

            val newStorageType = newStoragePreference.asStorageType
            var oldStorageType = Storage.Type.INTERNAL
            var oldStorage = App.instance.internalStorage
            if (newStorageType == Storage.Type.INTERNAL) {
                oldStorageType = Storage.Type.SDCARD
                oldStorage = App.instance.sdcardStorage!!
            }

            // clean up before
            oldStorage.clean()

            val fileCount = oldStorage.file.fileCount
            if (fileCount > 0) {
                val dialog = StorageMigrationDialogAutoBundle.builder(oldStorageType).build()
                dialog.listener = object : StorageMigrationDialog.Listener {
                    override fun onDialogPositiveClick() {
                        val progressDialog = ProgressDialogHorizontalAutoBundle.builder()
                            .title(getString(R.string.dialog_storage_migration_title))
                            .message(getString(R.string.dialog_storage_migration_message))
                            .build()
                        progressDialog.max = fileCount
                        progressDialog.show(fragmentManager!!, ProgressDialogHorizontal.TAG)

                        val migrationCallback = object : Storage.MigrationCallback {
                            override fun onProgressChanged(count: Int) {
                                activity?.runOnUiThread { progressDialog.progress = count }
                            }

                            override fun onCompleted(success: Boolean) {
                                activity?.runOnUiThread {
                                    if (success) {
                                        showToast(R.string.dialog_storage_migration_successful)
                                    } else {
                                        showToast(R.string.error_plain)
                                    }
                                    progressDialog.dismiss()
                                }
                            }
                        }

                        if (newStorageType == Storage.Type.INTERNAL) {
                            App.instance.sdcardStorage!!.migrateTo(
                                App.instance.internalStorage,
                                migrationCallback
                            )
                        } else {
                            App.instance.internalStorage.migrateTo(
                                App.instance.sdcardStorage!!,
                                migrationCallback
                            )
                        }
                    }
                }


                dialog.show(fragmentManager!!, StorageMigrationDialog.TAG)
            }
        }
    }

    override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)

        val c = activity ?: return
        val prefs = PreferenceManager.getDefaultSharedPreferences(c)

        findPreference<ListPreference>(getString(R.string.preference_storage))?.summary =
            prefs.getString(
                getString(R.string.preference_storage),
                getString(R.string.settings_default_value_storage)
            )!!
        findPreference<ListPreference>(getString(R.string.preference_storage))?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                FileDownloadHandler.isDownloadingAnything { isDownloadingAnything ->
                    if (isDownloadingAnything) {
                        showToast(R.string.notification_storage_locked)
                    } else {
                        val listener = preference.onPreferenceChangeListener
                        preference.onPreferenceChangeListener = null
                        (preference as ListPreference).value = newValue as String
                        preference.onPreferenceChangeListener = listener
                    }
                }
                false
            }

        if (App.instance.storages.size < 2) {
            findPreference<PreferenceCategory>(getString(R.string.preference_category_general))?.let { general ->
                findPreference<ListPreference>(getString(R.string.preference_storage))?.let { storagePref ->
                    general.removePreference(storagePref)
                }
            }
        }

        val pipSettings = findPreference<Preference>(getString(R.string.preference_video_pip))
        if (!Feature.PIP) {
            findPreference<PreferenceCategory>(getString(R.string.preference_category_video_playback_speed))?.let { video ->
                pipSettings?.let {
                    video.removePreference(it)
                }
            }
        } else {
            pipSettings?.setOnPreferenceClickListener {
                try {
                    val intent = Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS")
                    val uri = Uri.fromParts("package", activity?.packageName, null)
                    intent.data = uri
                    activity?.startActivity(intent)
                } catch (e: RuntimeException) {
                    PermissionManager.startAppInfo(activity)
                }
                true
            }
            refreshPipStatus()
        }

        // programmatically build info preferences

        val info = findPreference<PreferenceCategory>(getString(R.string.preference_category_info))!!

        val copyright = Preference(preferenceScreen.context)
        copyright.title = String.format(getString(R.string.settings_copyright), Calendar.getInstance().get(Calendar.YEAR))
        if (Feature.enabled("url_copyright")) {
            copyright.setOnPreferenceClickListener { _ ->
                openUrl(App.instance.getString("url_copyright"))
                true
            }
        } else {
            copyright.isEnabled = false
        }

        info.addPreference(copyright)

        if (Feature.enabled("legal_links_urls")) {
            val titles = App.instance.getStringArray("legal_links_titles")

            App.instance.getStringArray("legal_links_urls").forEachIndexed { i, url ->
                val pref = Preference(preferenceScreen.context)
                pref.title = titles[i]
                pref.setOnPreferenceClickListener { _ ->
                    openUrl(url)
                    true
                }

                info.addPreference(pref)
            }
        }

        if (Feature.enabled("url_faq")) {
            val faq = Preference(preferenceScreen.context)
            faq.title = getString(R.string.settings_title_faq)
            faq.setOnPreferenceClickListener { _ ->
                openUrl(App.instance.getString("url_faq"))
                true
            }

            info.addPreference(faq)
        }

        val licenses = Preference(preferenceScreen.context)
        licenses.title = getString(R.string.settings_title_licenses)
        licenses.summary = getString(R.string.settings_summary_licenses)
        licenses.setOnPreferenceClickListener {
            LicensesDialog.Builder(activity)
                .setNotices(R.raw.notices)
                .setTitle(R.string.settings_title_licenses)
                .build()
                .show()
            true
        }

        info.addPreference(licenses)

        val buildVersion = Preference(preferenceScreen.context)
        buildVersion.title = getString(R.string.settings_title_build)
        buildVersion.summary = getString(R.string.settings_summary_build) + " " + BuildConfig.VERSION_NAME
        buildVersion.isEnabled = false

        info.addPreference(buildVersion)

        loginOut = Preference(preferenceScreen.context)
        if (UserManager.isAuthorized) {
            buildLogoutView(loginOut)
        } else {
            buildLoginView(loginOut)
        }

        loginOut?.let {
            info.addPreference(it)
        }
    }

    private fun refreshPipStatus() {
        val pipSettings = findPreference<Preference>(getString(R.string.preference_video_pip))
        pipSettings?.let {
            if (!PermissionManager.hasPipPermission(context)) {
                it.summary = getString(R.string.settings_summary_video_pip_unavailable)
            } else {
                it.summary = ""
            }
        }
    }

    private fun buildLoginView(pref: Preference?) {
        if (pref != null) {
            pref.title = getString(R.string.login)
            pref.setOnPreferenceClickListener { _ ->
                val intent = LoginHelper.loginIntent(requireActivity())
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
                showToast(R.string.toast_successful_logout)
                true
            }
        }
    }

    private fun openUrl(url: String) {
        val customTabsIntent = CustomTabsIntent.Builder()
            .setToolbarColor(ContextCompat.getColor(App.instance, R.color.apptheme_primary))
            .build()
        customTabsIntent.launchUrl(activity!!, Uri.parse(url))
    }
}
