package de.xikolo.controllers.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.preference.*
import de.psdev.licensesdialog.LicensesDialog
import de.xikolo.App
import de.xikolo.BuildConfig
import de.xikolo.R
import de.xikolo.config.Config
import de.xikolo.config.FeatureConfig
import de.xikolo.controllers.dialogs.ProgressDialogHorizontal
import de.xikolo.controllers.dialogs.ProgressDialogHorizontalAutoBundle
import de.xikolo.controllers.dialogs.StorageMigrationDialog
import de.xikolo.controllers.dialogs.StorageMigrationDialogAutoBundle
import de.xikolo.controllers.login.LoginActivityAutoBundle
import de.xikolo.extensions.observe
import de.xikolo.managers.PermissionManager
import de.xikolo.managers.UserManager
import de.xikolo.models.Storage
import de.xikolo.services.DownloadService
import de.xikolo.utils.extensions.*
import java.util.*

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
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        refreshPipStatus()
        super.onResume()
    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
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

        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)

        findPreference<ListPreference>(getString(R.string.preference_storage))?.summary = prefs.getString(getString(R.string.preference_storage), getString(R.string.settings_default_value_storage))!!
        findPreference<ListPreference>(getString(R.string.preference_storage))?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, _ ->
                if (DownloadService.instance?.isDownloading == true) {
                    showToast(R.string.notification_storage_locked)
                    return@OnPreferenceChangeListener false
                }
                true
            }

        if (App.instance.storages.size < 2) {
            val general = findPreference<PreferenceCategory>(getString(R.string.preference_category_general))
            val storagePref = findPreference<ListPreference>(getString(R.string.preference_storage))
            general?.removePreference(storagePref)
        }

        val pipSettings = findPreference<Preference>(getString(R.string.preference_video_pip))
        if (!FeatureConfig.PIP) {
            val video = findPreference<PreferenceCategory>(getString(R.string.preference_category_video_playback_speed))
            video?.removePreference(pipSettings)
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

        val copyright = findPreference<Preference>(getString(R.string.preference_copyright))
        copyright?.title = String.format(copyright?.title.toString(), Calendar.getInstance().get(Calendar.YEAR))
        copyright?.setOnPreferenceClickListener { _ ->
            openUrl(Config.COPYRIGHT_URL)
            true
        }

        val imprint = findPreference<Preference>(getString(R.string.preference_imprint))
        if (Config.IMPRINT_URL != null) {
            imprint?.setOnPreferenceClickListener { _ ->
                openUrl(Config.IMPRINT_URL)
                true
            }
        } else {
            val info = findPreference<PreferenceCategory>(getString(R.string.preference_category_info))
            info?.removePreference(imprint)
        }

        val privacy = findPreference<Preference>(getString(R.string.preference_privacy))
        if (Config.PRIVACY_URL != null) {
            privacy?.setOnPreferenceClickListener { _ ->
                openUrl(Config.PRIVACY_URL)
                true
            }
        } else {
            val info = findPreference<PreferenceCategory>(getString(R.string.preference_category_info))
            info?.removePreference(privacy)
        }

        val termsOfUse = findPreference<Preference>(getString(R.string.preference_terms_of_use))
        if (Config.TERMS_OF_USE_URL != null) {
            termsOfUse?.setOnPreferenceClickListener { _ ->
                openUrl(Config.TERMS_OF_USE_URL)
                true
            }
        } else {
            val info = findPreference<PreferenceCategory>(getString(R.string.preference_category_info))
            info?.removePreference(termsOfUse)
        }

        val buildVersion = findPreference<Preference>(getString(R.string.preference_build_version))
        buildVersion?.summary = (buildVersion?.summary.toString()
            + " "
            + BuildConfig.VERSION_NAME)

        val licenses = findPreference<Preference>(getString(R.string.preference_open_source_licenses))
        licenses?.setOnPreferenceClickListener { _ ->
            LicensesDialog.Builder(activity)
                .setNotices(R.raw.notices)
                .setTitle(R.string.settings_title_licenses)
                .build()
                .show()
            true
        }

        loginOut = findPreference(getString(R.string.preference_login_out))
        if (UserManager.isAuthorized) {
            buildLogoutView(loginOut)
        } else {
            buildLoginView(loginOut)
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
                val intent = LoginActivityAutoBundle.builder().build(activity!!)
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
            .setToolbarColor(ContextCompat.getColor(App.instance, R.color.apptheme_main))
            .build()
        customTabsIntent.launchUrl(activity!!, Uri.parse(url))
    }
}
